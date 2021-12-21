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

package com.tle.core.auditlog.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.beans.Institution;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.auditlog.AuditLogDao;
import com.tle.core.auditlog.AuditLogExtension;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

/** Generic audit logging service. */
@Bind(AuditLogService.class)
@Singleton
@SuppressWarnings("nls")
public class AuditLogServiceImpl implements AuditLogService {
  private static final String USER_CATEGORY = "USER";
  private static final String ENTITY_CATEGORY = "ENTITY";
  private static final String SEARCH_CATEGORY = "SEARCH";
  private static final String ITEM_CATEGORY = "ITEM";
  private static final String EXTERNAL_CONN_CATEGORY = "EXTERNAL_CONNECTOR";

  private static final String CREATED_TYPE = "CREATED";
  private static final String MODIFIED_TYPE = "MODIFIED";
  private static final String DELETED_TYPE = "DELETED";
  private static final String PURGED_TYPE = "PURGED";
  private static final String CONTENT_VIEWED_TYPE = "CONTENT_VIEWED";
  private static final String SUMMARY_VIEWED_TYPE = "SUMMARY_VIEWED";

  private static final String SEARCH_FEDERATED_TYPE = "FEDERATED";
  private static final String SEARCH_EXPORT_TYPE = "EXPORT";

  private static final String USED_TYPE = "USED";

  @Inject private AuditLogDao dao;
  private PluginTracker<AuditLogExtension> extensionTracker;

  @Override
  @Transactional
  public void removeOldLogs(int daysOld) {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.HOUR_OF_DAY, 23);
    c.set(Calendar.MINUTE, 59);
    c.add(Calendar.DAY_OF_YEAR, -daysOld);

    Date date = c.getTime();
    dao.removeEntriesBeforeDate(date);
    for (AuditLogExtension extension : getExtensions()) {
      extension.getDao().removeEntriesBeforeDate(date);
    }
  }

  private void logUserEvent(String type, UserState us, HttpServletRequest request) {
    UserBean ub = us.getUserBean();
    logWithRequest(
        USER_CATEGORY,
        type,
        us.getIpAddress(),
        ub.getUniqueID(),
        ub.getUsername(),
        us.getTokenSecretId(),
        request);
  }

  @Override
  @Transactional
  public void logUserLoggedIn(UserState us, HttpServletRequest request) {
    logUserEvent("LOGIN", us, request);
  }

  @Override
  @Transactional
  public void logUserFailedAuthentication(String username, WebAuthenticationDetails wad) {
    logGeneric(USER_CATEGORY, "AUTH ERROR", wad.getIpAddress(), username, "BAD CREDENTIALS", null);
  }

  @Override
  @Transactional
  public void logUserLoggedOut(UserState us, HttpServletRequest request) {
    logUserEvent("LOGOUT", us, request);
  }

  @Override
  @Transactional
  public void logEntityCreated(long entityId) {
    logEntityGeneric(CREATED_TYPE, entityId);
  }

  @Override
  @Transactional
  public void logEntityModified(long entityId) {
    logEntityGeneric(MODIFIED_TYPE, entityId);
  }

  @Override
  @Transactional
  public void logEntityDeleted(long entityId) {
    logEntityGeneric(DELETED_TYPE, entityId);
  }

  @Override
  @Transactional
  public void logObjectDeleted(long objectId, String friendlyName) {
    logGeneric(
        friendlyName, DELETED_TYPE, CurrentUser.getUserID(), Long.toString(objectId), null, null);
  }

  @Override
  @Transactional
  public void logSummaryViewed(String category, ItemKey item, HttpServletRequest request) {
    logWithRequest(
        category,
        SUMMARY_VIEWED_TYPE,
        item.getUuid(),
        Integer.toString(item.getVersion()),
        null,
        null,
        request);
  }

  @Override
  @Transactional
  public void logItemSummaryViewed(Item item, HttpServletRequest request) {
    logSummaryViewed(ITEM_CATEGORY, item.getItemId(), request);
  }

  @Override
  @Transactional
  public void logContentViewed(
      String category,
      ItemKey itemId,
      String contentType,
      String path,
      HttpServletRequest request) {
    logWithRequest(
        category,
        CONTENT_VIEWED_TYPE,
        itemId.getUuid(),
        Integer.toString(itemId.getVersion()),
        contentType,
        path,
        request);
  }

  @Override
  @Transactional
  public void logItemContentViewed(
      ItemKey itemId,
      String contentType,
      String path,
      IAttachment attachment,
      HttpServletRequest request) {
    logContentViewed(ITEM_CATEGORY, itemId, contentType, path, request);
  }

  @Override
  @Transactional
  public void logItemPurged(Item item) {
    logGeneric(
        ITEM_CATEGORY,
        PURGED_TYPE,
        item.getUuid(),
        Integer.toString(item.getVersion()),
        null,
        null);
  }

  @Override
  public void logExternalConnectorUsed(
      String externalConnectorUrl,
      String requestLimit,
      String requestRemaining,
      String timeToReset) {
    logGeneric(
        EXTERNAL_CONN_CATEGORY,
        USED_TYPE,
        externalConnectorUrl,
        requestLimit,
        requestRemaining,
        timeToReset);
  }

  private void logEntityGeneric(String type, long entityId) {
    logGeneric(ENTITY_CATEGORY, type, CurrentUser.getUserID(), Long.toString(entityId), null, null);
  }

  @Override
  @Transactional
  public void logSearch(String type, String freeText, String within, long resultCount) {
    logGeneric(SEARCH_CATEGORY, type, freeText, within, Long.toString(resultCount), null);
  }

  @Override
  @Transactional
  public void logSearchExport(String format, String searchParams) {
    // searchParams could be a long string so use 'data4'.
    logGeneric(SEARCH_CATEGORY, SEARCH_EXPORT_TYPE, format, null, null, searchParams);
  }

  @Override
  public void logWithRequest(
      String category,
      String type,
      String d1,
      String d2,
      String d3,
      String d4,
      HttpServletRequest request) {

    ObjectMapper mapper = new ObjectMapper();
    HttpRequestMeta referer = new HttpRequestMeta(request);
    String meta = "";
    try {
      meta = mapper.writeValueAsString(referer);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to build a JSON string for meta of audit log.");
    }

    AuditLogEntry entry =
        new AuditLogEntry(
            CurrentUser.getUserID(),
            CurrentUser.getSessionID(),
            category,
            type,
            new Date(),
            d1,
            d2,
            d3,
            d4,
            CurrentInstitution.get(),
            meta);

    dao.save(entry);
  }

  @Override
  @Transactional
  public void logFederatedSearch(String freeText, String searchId) {
    logGeneric(SEARCH_CATEGORY, SEARCH_FEDERATED_TYPE, freeText, searchId, null, null);
  }

  @Override
  @Transactional
  public void logGeneric(String category, String type, String d1, String d2, String d3, String d4) {
    log(
        CurrentUser.getUserID(),
        CurrentUser.getSessionID(),
        category,
        type,
        d1,
        d2,
        d3,
        d4,
        CurrentInstitution.get());
  }

  private void log(
      String userId,
      String sessionId,
      String category,
      String type,
      String d1,
      String d2,
      String d3,
      String d4,
      Institution institution) {
    dao.save(
        new AuditLogEntry(
            userId, sessionId, category, type, new Date(), d1, d2, d3, d4, institution, null));
  }

  @Override
  @Transactional
  public Collection<AuditLogExtension> getExtensions() {
    return extensionTracker.getBeanList();
  }

  @Inject
  public void setPluginService(PluginService pluginService) {
    extensionTracker =
        new PluginTracker<AuditLogExtension>(
                pluginService, "com.tle.core.auditlog", "auditTable", null)
            .setBeanKey("bean");
  }

  @Override
  @Transactional
  public void removeEntriesForInstitution(Institution institution) {
    dao.removeEntriesForInstitution(institution);
    for (AuditLogExtension extension : getExtensions()) {
      extension.getDao().removeEntriesForInstitution(institution);
    }
  }

  @Override
  @Transactional
  public void removeEntriesForUser(String userId) {
    dao.removeEntriesForUser(userId);
  }

  @Override
  public int countByInstitution(Institution institution) {
    return (int) dao.countByCriteria(dao.restrictByInstitution(institution));
  }

  @Override
  public List<AuditLogEntry> findAllByInstitution(
      Order order, int firstResult, int maxResults, Institution institution) {
    return dao.findAllByCriteria(
        order, firstResult, maxResults, dao.restrictByInstitution(institution));
  }

  @Override
  public List<AuditLogEntry> findByUser(String userId) {
    return dao.findAllByCriteria(
        Restrictions.eq("institution", CurrentInstitution.get()),
        Restrictions.eq("userId", userId));
  }

  /** Class which provides metadata of HTTP request. */
  static class HttpRequestMeta {
    private String referer;

    public HttpRequestMeta(HttpServletRequest request) {
      this.referer = request.getHeader("Referer");
    }

    public String getReferer() {
      return referer;
    }

    public void setReferer(String referer) {
      this.referer = referer;
    }
  }
}
