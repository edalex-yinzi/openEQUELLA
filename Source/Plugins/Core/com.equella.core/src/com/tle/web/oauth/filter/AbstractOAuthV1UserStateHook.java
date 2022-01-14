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

package com.tle.web.oauth.filter;

import com.dytech.edge.exceptions.WebException;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.services.UrlService;
import com.tle.core.services.user.UserService;
import com.tle.web.core.filter.UserStateHook;
import com.tle.web.core.filter.UserStateResult;
import com.tle.web.core.filter.UserStateResult.Result;
import com.tle.web.oauth.OAuthWebConstants;
import com.tle.web.oauth.service.OAuthWebService;
import com.tle.web.sections.equella.annotation.PlugKey;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOAuthV1UserStateHook implements UserStateHook {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOAuthV1UserStateHook.class);

  @PlugKey("oauth.v1.error.")
  protected static String OAUTH_ERROR_PREFIX;

  @Inject private OAuthWebService oauthWebService;
  @Inject private UserService userService;
  @Inject protected UrlService urlService;

  @Override
  public UserStateResult getUserState(HttpServletRequest request, UserState userState)
      throws WebException {
    UserStateResult returnedResult = null;
    String sig = request.getParameter(OAuthWebConstants.PARAM_SIGNATURE);
    if (!Check.isEmpty(sig)) {
      String consumerKey = request.getParameter(OAuthWebConstants.PARAM_CONSUMER_KEY);

      if (!Check.isEmpty(consumerKey)) {
        try {
          String secret = getSecretFromKey(consumerKey);
          if (secret == null) {
            return null;
          }

          oauthWebService.validateMessage(
              getOAuthMessage(request),
              new OAuthAccessor(new OAuthConsumer("about:blank", consumerKey, secret, null)));

          returnedResult = getUserStateResult(request);
        } catch (OAuthException e) {
          LOGGER.error("context", e);
          String error = e.getMessage();
          Integer code = OAuth.Problems.TO_HTTP_CODE.get(error);
          throw new WebException(code, error, CurrentLocale.get(OAUTH_ERROR_PREFIX + error));
        } catch (URISyntaxException e) {
          LOGGER.error("context", e);
          String error = e.getMessage();
          throw new WebException(400, error, error);
        } catch (IOException e) {
          LOGGER.error("context", e);
          String error = e.getMessage();
          throw new WebException(500, error, error);
        }
      } else {
        throw new WebException(400, "badoauth", "OAuth request is signed without a consumer key");
      }
    }

    return returnedResult;
  }

  protected UserStateResult getUserStateResult(HttpServletRequest request) {
    return new UserStateResult(userService.authenticateRequest(request), Result.LOGIN_SESSION);
  }

  protected abstract OAuthMessage getOAuthMessage(HttpServletRequest request);

  protected abstract String getSecretFromKey(String key);

  @Override
  public boolean isInstitutional() {
    return true;
  }
}
