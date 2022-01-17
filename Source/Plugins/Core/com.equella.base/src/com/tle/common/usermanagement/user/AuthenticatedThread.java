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

package com.tle.common.usermanagement.user;

import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import java.util.Stack;
import org.apache.log4j.NDC;

/**
 * Copies over security context from a calling thread to the new thread.
 *
 * @author Nicholas Read
 */
public abstract class AuthenticatedThread extends Thread {
  private UserState callingThreadsAuthentication;
  private Institution callingThreadsInstitution;
  private Stack<String> loggingContext;

  public AuthenticatedThread() {
    super();
    setup();
  }

  public AuthenticatedThread(String name) {
    super(name);
    setup();
  }

  public AuthenticatedThread(ThreadGroup group, String name) {
    super(group, name);
    setup();
  }

  private void setup() {
    callingThreadsAuthentication = CurrentUser.getUserState();
    callingThreadsInstitution = CurrentInstitution.get();
    loggingContext = NDC.cloneStack();
  }

  @Override
  public final void run() {
    try {
      NDC.inherit(loggingContext);
      CurrentUser.setUserState(callingThreadsAuthentication);
      CurrentInstitution.set(callingThreadsInstitution);
      doRun();
    } finally {
      CurrentInstitution.remove();
      CurrentUser.setUserState(null);
      NDC.remove();
    }
  }

  public abstract void doRun();
}
