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

package com.tle.web.search.base;

import com.dytech.edge.exceptions.InvalidSearchQueryException;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.LiveItemSearch;
import com.tle.common.search.PresetSearch;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TreeIndexed
public abstract class AbstractFreetextResultsSection<
        LE extends AbstractItemListEntry, M extends SearchResultsModel>
    extends AbstractSearchResultsSection<LE, FreetextSearchEvent, FreetextSearchResultEvent, M> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractFreetextResultsSection.class);

  @PlugKey("search.invalidfreetext")
  private static Label LABEL_INVALIDFREETEXT;

  @Inject private IntegrationService integrationService;
  @Inject private FreeTextService freeText;
  @Inject private UserSessionService sessionService;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    registerItemList(tree, id);
  }

  protected abstract void registerItemList(SectionTree tree, String id);

  @Override
  public abstract AbstractItemList<LE, ?> getItemList(SectionInfo info);

  @Override
  public void processResults(SectionInfo info, FreetextSearchResultEvent event) {
    FreetextSearchResults<? extends FreetextResult> results = event.getResults();
    FreetextSearchEvent searchEvent = event.getSearchEvent();

    DefaultSearch finalSearch = searchEvent.getFinalSearch();
    Collection<String> words =
        new DefaultSearch.QueryParser(searchEvent.getSearchedText()).getHilightedList();

    if (finalSearch instanceof PresetSearch) {
      PresetSearch presetSearch = (PresetSearch) finalSearch;
      boolean dynamicCollection = presetSearch.isDynamicCollection();
      String queryText = presetSearch.getQueryText();
      if (dynamicCollection) {
        // remove dynamic collection filter freetext search word from
        // the highlighted list
        words.remove(queryText);
      }
    }

    AbstractItemList<LE, ?> itemList = getItemList(info);
    ListSettings<LE> settings = itemList.getListSettings(info);
    customiseSettings(info, settings);
    settings.setHilightedWords(words);
    int count = results.getCount();
    boolean flagListAsNullItemsRemoved = false;

    // Ensure the NullItemsRemoved flag is cleared: may have been set in a
    // previous iteration
    itemList.setNullItemsRemovedOnModel(info, false);

    for (int i = 0; i < count; i++) {
      Item item = results.getItem(i);
      if (item != null) {
        itemList.addItem(
            info, item, results.getResultData(i), i + results.getOffset(), results.getAvailable());
      } else {
        flagListAsNullItemsRemoved = true;
      }
    }

    // nullItemsRemoved flag set as a property of the model (so as to be
    // accessible by the ftl)
    if (flagListAsNullItemsRemoved) {
      itemList.setNullItemsRemovedOnModel(info, true);
    }
  }

  protected void customiseSettings(SectionInfo info, ListSettings<LE> settings) {
    // for subclasses
  }

  @Override
  protected List<Label> getErrorMessageLabels(
      SectionInfo info, FreetextSearchEvent searchEvent, FreetextSearchResultEvent resultsEvent) {
    Throwable exception = resultsEvent.getException();
    List<Label> errorLabels = new ArrayList<Label>();
    if (exception instanceof InvalidSearchQueryException) {
      errorLabels.add(LABEL_INVALIDFREETEXT);
    } else {
      errorLabels.add(new TextLabel(exception.getMessage()));
    }

    return errorLabels;
  }

  @Override
  public FreetextSearchEvent createSearchEvent(SectionInfo info) {
    DefaultSearch[] searches = createSearches(info);
    String priv = getSelectionService().getSearchPrivilege(info);
    searches[0].setPrivilege(priv);
    searches[1].setPrivilege(priv);
    return new FreetextSearchEvent(searches[0], searches[1]);
  }

  protected DefaultSearch[] createSearches(SectionInfo info) {
    return new DefaultSearch[] {createDefaultSearch(info), createDefaultSearch(info)};
  }

  protected DefaultSearch createDefaultSearch(SectionInfo info) {
    return new LiveItemSearch();
  }

  @Override
  protected FreetextSearchResultEvent createResultsEvent(
      SectionInfo info, FreetextSearchEvent searchEvent) {
    try {
      if (searchEvent.getException() != null) {
        throw searchEvent.getException();
      }
      int[] count =
          freeText.countsFromFilters(Collections.singleton(searchEvent.getUnfilteredSearch()));
      FreetextSearchResults<FreetextResult> results =
          freeText.search(
              searchEvent.getFinalSearch(), searchEvent.getOffset(), searchEvent.getCount());
      return new FreetextSearchResultEvent(results, searchEvent, count[0] - results.getAvailable());
    } catch (Exception t) {
      LOGGER.error("Error searching", t); // $NON-NLS-1$
      return new FreetextSearchResultEvent(t, searchEvent);
    }
  }

  public ItemKey getResultForIndex(SectionInfo info, int index) {
    FreetextSearchEvent searchEvent = createSearchEvent(info);
    info.processEvent(searchEvent);
    searchEvent.setOffset(index);
    searchEvent.setCount(1);
    FreetextSearchResults<? extends FreetextResult> results =
        createResultsEvent(info, searchEvent).getResults();
    if (results.getCount() > 0) {
      return results.getItemKey(0);
    }
    return null;
  }
}
