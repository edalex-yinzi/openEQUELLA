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

package com.tle.web.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nullable;

public abstract class WrappedContentStream implements ContentStream {
  protected ContentStream inner;

  // Set 'max-age' to 0 so that the content of cache is always stale.
  // Hence, the browser sends every request to the the Equella server which will then
  // re-validate the content.
  private final String defaultCacheControl = "max-age=0, s-maxage=0, must-revalidate";

  public WrappedContentStream(ContentStream inner) {
    this.inner = inner;
  }

  @Override
  public boolean exists() {
    return inner.exists();
  }

  @Override
  public String getContentDisposition() {
    return inner.getContentDisposition();
  }

  @Override
  public long getContentLength() {
    return inner.getContentLength();
  }

  @Nullable
  @Override
  public String calculateETag() {
    return inner.calculateETag();
  }

  @Override
  public long getEstimatedContentLength() {
    return getContentLength();
  }

  @Override
  public File getDirectFile() {
    return inner.getDirectFile();
  }

  @Override
  public String getFilenameWithoutPath() {
    return inner.getFilenameWithoutPath();
  }

  @Override
  public long getLastModified() {
    return inner.getLastModified();
  }

  @Override
  public String getMimeType() {
    return inner.getMimeType();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return inner.getInputStream();
  }

  public boolean useDefaultCacheControl() {
    return false;
  }

  @Override
  public String getCacheControl() {
    return useDefaultCacheControl() ? defaultCacheControl : inner.getCacheControl();
  }

  @Override
  public void setCacheControl(String cacheControl) {
    inner.setCacheControl(cacheControl);
  }

  @Override
  public boolean mustWrite() {
    return inner.mustWrite();
  }

  @Override
  public void write(OutputStream out) throws IOException {
    inner.write(out);
  }
}
