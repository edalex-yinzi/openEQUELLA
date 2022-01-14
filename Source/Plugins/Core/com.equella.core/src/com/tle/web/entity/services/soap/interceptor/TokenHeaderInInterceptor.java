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

package com.tle.web.entity.services.soap.interceptor;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

@SuppressWarnings("nls")
@Bind
@Singleton
public class TokenHeaderInInterceptor extends AbstractSoapInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenHeaderInInterceptor.class);

  @Inject private UserService userService;

  public TokenHeaderInInterceptor() {
    super(Phase.POST_PROTOCOL);
  }

  @Override
  public void handleMessage(SoapMessage message) throws Fault {
    Header header = message.getHeader(new QName("equella"));
    if (header != null) {
      Element e = (Element) header.getObject();
      PropBagEx equella = new PropBagEx(e);
      String token = equella.getNode("token");
      if (!Check.isEmpty(token)) {
        HttpServletRequest request =
            (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        try {
          userService.loginWithToken(token, userService.getWebAuthenticationDetails(request), true);
        } catch (RuntimeException ex) {
          LOGGER.error(
              "Error initialising session with SOAP header token '"
                  + token
                  + "' for URL "
                  + request.getRequestURL().toString());
          throw ex;
        }
      }
    }
  }
}
