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

package com.tle.web.core.listener;

import com.dytech.edge.web.WebConstants;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.events.UserSessionLogoutEvent;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.services.user.UserSessionService;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bind
@Singleton
public class UserSessionDestructionListener implements HttpSessionListener {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(UserSessionDestructionListener.class);

  @Inject private UserSessionService sessionService;
  @Inject private InstitutionService institutionService;
  @Inject private EventService eventService;
  @Inject private RunAsInstitution runAs;

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    // We don't care about these
    if (LOGGER.isDebugEnabled()) {
      final String sessionId = event.getSession().getId();
      LOGGER.debug(sessionId + " session created");
    }
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    final HttpSession session = event.getSession();
    final String sessionId = session.getId();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(sessionId + " session destroyed");
    }

    try {
      for (final Institution institution : institutionService.getAvailableMap().values()) {
        runAs.executeAsSystem(
            institution,
            new Runnable() {

              @Override
              public void run() {
                UserState userState =
                    sessionService.getAttributeFromSession(
                        session, institution, WebConstants.KEY_USERSTATE);
                if (userState != null) {
                  if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(sessionId + " firing logout event");
                  }
                  eventService.publishApplicationEvent(new UserSessionLogoutEvent(userState, true));
                }
              }
            });
      }
    } finally {
      CurrentInstitution.remove();
    }
  }
}
