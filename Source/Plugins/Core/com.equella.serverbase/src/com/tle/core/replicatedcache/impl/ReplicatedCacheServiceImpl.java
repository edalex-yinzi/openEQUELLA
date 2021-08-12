/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.replicatedcache.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.tle.annotation.NonNull;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.ExpiringValue;
import com.tle.common.Pair;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.dao.helpers.BatchingIterator;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.events.InstitutionEvent;
import com.tle.core.institution.events.listeners.InstitutionListener;
import com.tle.core.plugins.PluginAwareObjectInputStream;
import com.tle.core.plugins.PluginAwareObjectOutputStream;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.dao.CachedValue;
import com.tle.core.replicatedcache.dao.ReplicatedCacheDao;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.zookeeper.ZookeeperService;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Singleton
@NonNullByDefault
@SuppressWarnings("nls")
@Bind(ReplicatedCacheService.class)
public class ReplicatedCacheServiceImpl
    implements ReplicatedCacheService,
        ReplicatedCacheInvalidationListener,
        InstitutionListener,
        ScheduledTask {
  @Inject private ReplicatedCacheDao dao;
  @Inject private EventService eventService;
  @Inject private ZookeeperService zookeeperService;

  /** Caches identified by cacheId. */
  private final Cache<String, ReplicatedCacheImpl<?>> caches = CacheBuilder.newBuilder().build();

  private static final Log LOGGER = LogFactory.getLog(ReplicatedCacheServiceImpl.class);

  @Override
  public synchronized <V extends Serializable> ReplicatedCache<V> getCache(
      String cacheId, long maxLocalCacheSize, long ttl, TimeUnit ttlUnit) {
    return getCache(cacheId, maxLocalCacheSize, ttl, ttlUnit, false);
  }

  @Override
  public synchronized <V extends Serializable> ReplicatedCache<V> getCache(
      String cacheId, long maxLocalCacheSize, long ttl, TimeUnit ttlUnit, boolean alwaysPersist) {
    Preconditions.checkNotNull(cacheId, "cacheId cannot be null");

    @SuppressWarnings("unchecked")
    ReplicatedCacheImpl<V> cache = (ReplicatedCacheImpl<V>) caches.getIfPresent(cacheId);
    if (cache == null) {
      cache = new ReplicatedCacheImpl<V>(cacheId, maxLocalCacheSize, ttl, ttlUnit, alwaysPersist);
      caches.put(cacheId, cache);
    }
    return cache;
  }

  @NonNullByDefault
  private class ReplicatedCacheImpl<V extends Serializable> implements ReplicatedCache<V> {
    private final String cacheId;
    private final LoadingCache<Institution, LoadingCache<String, Optional<ExpiringValue<V>>>> cache;
    private final long ttl;
    private final TimeUnit ttlUnit;
    private final boolean alwaysPersist;

    public ReplicatedCacheImpl(
        String cacheId,
        final long maxLocalCacheSize,
        final long ttl,
        final TimeUnit ttlUnit,
        boolean alwaysPersist) {
      this.cacheId = checkNotNull(cacheId);
      this.ttl = ttl;
      this.ttlUnit = ttlUnit;
      this.alwaysPersist = alwaysPersist;
      // The in-memory caches at both the institution and key/value levels
      // expire after 1 day of not being accessed. No use holding stuff in
      // memory that we're not using.
      cache =
          CacheBuilder.newBuilder()
              .expireAfterAccess(1, TimeUnit.DAYS)
              .build(
                  new CacheLoader<Institution, LoadingCache<String, Optional<ExpiringValue<V>>>>() {
                    @Override
                    public LoadingCache<String, Optional<ExpiringValue<V>>> load(Institution key)
                        throws Exception {
                      return CacheBuilder.newBuilder()
                          .maximumSize(maxLocalCacheSize)
                          .softValues()
                          .expireAfterWrite(ttl, ttlUnit)
                          .expireAfterAccess(1, TimeUnit.DAYS)
                          .build(
                              new CacheLoader<String, Optional<ExpiringValue<V>>>() {
                                @Override
                                public Optional<ExpiringValue<V>> load(String key)
                                    throws Exception {
                                  if (!zookeeperService.isCluster() && !alwaysPersist) {
                                    return Optional.absent();
                                  }

                                  CachedValue cv = dao.get(ReplicatedCacheImpl.this.cacheId, key);

                                  // Return Absent so we don't keep looking it
                                  // up.
                                  if (cv == null) {
                                    return Optional.absent();
                                  }

                                  @SuppressWarnings("unchecked")
                                  V v = (V) PluginAwareObjectInputStream.fromBytes(cv.getValue());
                                  return Optional.of(
                                      ExpiringValue.expireAt(v, cv.getTtl().getTime()));
                                }
                              });
                    }
                  });
    }

    @Override
    public synchronized Optional<V> get(@NonNull String key) {
      checkNotNull(key);
      if (alwaysPersist || zookeeperService.isCluster()) {
        cache.refresh(CurrentInstitution.get());
      }

      LoadingCache<String, Optional<ExpiringValue<V>>> c;
      try {
        c = cache.get(CurrentInstitution.get());
        Optional<ExpiringValue<V>> op = c.getUnchecked(key);
        if (op.isPresent()) {
          V ev = op.get().getValue();
          return Optional.fromNullable(ev);
        }

      } catch (ExecutionException e) {
        LOGGER.error("Fail to access cache of institution " + CurrentInstitution.get());
      }
      return Optional.absent();
    }

    @Override
    public synchronized void put(@NonNull String key, @NonNull V value) {
      put(key, value, Instant.ofEpochMilli(System.currentTimeMillis() + ttlUnit.toMillis(ttl)));
    }

    @Override
    public synchronized void put(@NonNull String key, @NonNull V value, Instant dbEntryTTL) {
      checkNotNull(key);
      checkNotNull(value);

      LoadingCache<String, Optional<ExpiringValue<V>>> c =
          cache.getUnchecked(CurrentInstitution.get());

      // Do nothing if the value hasn't changed
      Optional<ExpiringValue<V>> opExVal = c.getIfPresent(key);
      if (opExVal != null && opExVal.isPresent()) {
        V oldValue = opExVal.get().getValue();
        if (oldValue != null && oldValue.equals(value)) {
          return;
        }
      }

      // Update the DB state if it's clustered
      if (alwaysPersist || zookeeperService.isCluster()) {
        dao.put(cacheId, key, Date.from(dbEntryTTL), PluginAwareObjectOutputStream.toBytes(value));
      }

      // Invalidate other servers caches
      invalidateOthers(key);

      // Update our local cache
      c.put(key, Optional.of(ExpiringValue.expireAfter(value, ttl, ttlUnit)));
    }

    @Override
    public synchronized void invalidate(@NonNull String... keys) {
      if (Check.isEmpty(keys)) {
        // Nothing to do
        return;
      }

      if (alwaysPersist || zookeeperService.isCluster()) {
        dao.invalidate(cacheId, keys);
      }

      invalidateOthers(keys);
      invalidateLocal(keys);
    }

    public void invalidateLocal(String... keys) {
      LoadingCache<String, ?> c = cache.getIfPresent(CurrentInstitution.get());
      if (c != null) {
        for (String key : keys) {
          c.invalidate(key);
        }
      }
    }

    private void invalidateOthers(String... keys) {
      eventService.publishApplicationEvent(new ReplicatedCacheInvalidationEvent(cacheId, keys));
    }

    @Override
    public Iterable<Pair<String, V>> iterate(final String keyPrefixFilter) {
      Iterable<CachedValue> iterable =
          new BatchingIterator<CachedValue>() {
            private static final int BATCH_SIZE = 200;

            @Override
            protected Iterator<CachedValue> getMore(Optional<CachedValue> lastObj) {
              long startId = lastObj.isPresent() ? lastObj.get().getId() : 1;
              return dao.getBatch(cacheId, keyPrefixFilter, startId, BATCH_SIZE).iterator();
            }
          };

      return Iterables.transform(
          iterable,
          new Function<CachedValue, Pair<String, V>>() {
            @Override
            @Nullable
            public Pair<String, V> apply(@Nullable CachedValue input) {
              @SuppressWarnings("unchecked")
              V v = (V) PluginAwareObjectInputStream.fromBytes(input.getValue());
              return new Pair<>(input.getKey(), v);
            }
          });
    }
  }

  @Override
  public void invalidateCacheEntries(String cacheId, String... keys) {
    ReplicatedCacheImpl<?> cache = caches.getIfPresent(cacheId);
    if (cache != null) {
      cache.invalidateLocal(keys);
    }
  }

  @Override
  public void institutionEvent(InstitutionEvent event) {
    switch (event.getEventType()) {
      case UNAVAILABLE:
      case DELETED:
        for (final Institution inst : event.getChanges().values()) {
          for (ReplicatedCacheImpl<?> rci : caches.asMap().values()) {
            rci.cache.invalidate(inst);
          }
        }
        break;
      case AVAILABLE:
        break;
      case EDITED:
        break;
      case STATUS:
        break;
      default:
        break;
    }
  }

  @Override
  public void execute() {
    // Clear expired keys from DB
    dao.invalidateExpiredEntries();
  }
}
