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

package com.dytech.edge.exceptions;

public class WebException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final int code;
  private final String error;

  /**
   * @param code
   * @param error A short string uniquely identifying the error
   * @param message
   */
  public WebException(int code, String error, String message) {
    super(message);
    this.error = error;
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  /**
   * @return A short string uniquely identifying the error
   */
  public String getError() {
    return error;
  }
}
