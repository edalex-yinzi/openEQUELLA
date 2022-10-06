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

package com.tle.web.myresources.selection;

import com.tle.core.guice.Bind;
import com.tle.web.myresources.RootMyResourcesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.AbstractSelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.template.RenderNewTemplate;
import java.util.Collections;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class MyResourcesSelectable extends AbstractSelectionNavAction {
  static {
    PluginResourceHandler.init(MyResourcesSelectable.class);
  }

  @PlugKey("label.myresources.action")
  private static Label LABEL_MYRESOURCES_ACTION;

  @Override
  public Label getLabelForNavAction(SectionInfo info) {
    return LABEL_MYRESOURCES_ACTION;
  }

  @Override
  public SectionInfo createForwardForNavAction(SectionInfo info, SelectionSession session) {
    return getMyResourcesTree(info);
  }

  protected SectionInfo getMyResourcesTree(SectionInfo info) {
    // The Old UI persists the previous My Resources tab you're on, however on each new visit to
    // My Resources in the New UI a standard default value is used. To allow the New UI code to look
    // after that, we send it to `defaultValue`.
    if (RenderNewTemplate.isNewUIEnabled()) {
      return info.createForwardForUri(
          RootMyResourcesSection.buildForwardUrl("defaultValue", Collections.emptyMap()));
    }
    return info.createForward(RootMyResourcesSection.URL);
  }

  @Override
  public boolean isActionAvailable(SectionInfo info, SelectionSession session) {
    if (!super.isActionAvailable(info, session)) {
      return false;
    }
    Layout layout = session.getLayout();
    return layout == Layout.SKINNY || layout == Layout.COURSE;
  }

  @Override
  public String getActionType() {
    return "myresources";
  }

  @Override
  public boolean isShowBreadcrumbs() {
    return true;
  }

  @Override
  public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session) {
    return getMyResourcesTree(info);
  }
}
