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

package com.tle.web.viewitem.attachments;

import com.tle.beans.item.attachments.Attachment;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.core.security.TLEAclManager;
import com.tle.core.url.URLCheckerService;
import com.tle.core.viewcount.service.ViewCountService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class LinkResource
    implements AttachmentResourceExtension<Attachment>, RegisterMimeTypeExtension<Attachment> {
  @Inject private URLCheckerService urlCheckerService;
  @Inject private TLEAclManager aclService;
  @Inject private ViewCountService viewCountService;

  @Override
  public ViewableResource process(
      SectionInfo info, ViewableResource resource, Attachment attachment) {
    return new DetailUrlResource(
        resource,
        attachment.getUrl(),
        attachment.getDescription(),
        urlCheckerService,
        aclService,
        viewCountService);
  }

  @Override
  public String getMimeType(Attachment attachment) {
    return MimeTypeConstants.MIME_LINK;
  }
}
