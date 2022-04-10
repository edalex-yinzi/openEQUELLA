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

package com.tle.web.viewitem;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.collection.AttachmentConfigConstants;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.security.SecurityConstants;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.login.LogonSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionsController;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.WrappedContentStream;
import com.tle.web.viewitem.service.FileFilterService;
import com.tle.web.viewurl.ViewAuditEntry;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Bind
@Singleton
public class ItemFilestoreServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Inject private FileSystemService fileSystemService;
  @Inject private ItemFileService itemFileService;
  @Inject private MimeTypeService mimeService;
  @Inject private ViewItemAuditor auditor;
  @Inject private ItemService itemService;
  @Inject private SectionsController controller;
  @Inject private ContentStreamWriter contentStreamWriter;
  @Inject private FileFilterService fileFilterService;

  private static PluginResourceHelper urlHelper =
      ResourcesService.getResourceHelper(ItemFilestoreServlet.class);

  @SuppressWarnings("nls")
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String path = request.getPathInfo();
    int firstPath = path.indexOf('/', 1);
    String uuid = path.substring(1, firstPath);
    int secondPath = path.indexOf('/', firstPath + 1);
    String version = path.substring(firstPath + 1, secondPath);
    path = path.substring(secondPath + 1);

    FileHandle fileHandle;
    ItemKey itemId = null;
    try {
      Attachment attachment = null;

      if (version.equals("$")) {
        fileHandle = new StagingFile(uuid);
      } else {
        itemId = ItemTaskId.parse(uuid + '/' + version);
        if (path.contains(FileSystemService.GALLERY_PREVIEW_EXTENSION)) {
          String sourcePath = path.replace(FileSystemService.GALLERY_PREVIEW_EXTENSION, "");
          sourcePath = sourcePath.replace(FileSystemService.THUMBS_FOLDER + "/", "");
          attachment = ((Attachment) getAttachmentWithDifferentPriv(itemId, sourcePath));
        } else {
          attachment = ((Attachment) getAttachmentWithDifferentPriv(itemId, path));
        }
        fileHandle = itemFileService.getItemFile(ItemId.fromKey(itemId), null);
        Set<String> privs = itemService.getCachedPrivileges(itemId);
        if (privs == null) {
          privs = itemService.getItemPrivsFast(itemId);
        }
        if (privs == null
            || (!privs.contains(SecurityConstants.VIEW_ITEM)
                && !privs.contains(AttachmentConfigConstants.VIEW_ATTACHMENTS))) {
          String privileges =
              SecurityConstants.VIEW_ITEM + ", " + AttachmentConfigConstants.VIEW_ATTACHMENTS;
          throw new AccessDeniedException(
              urlHelper.getString("viewitem.missingprivileges", privileges));
        }
        // check restricted attachments (FIXME: needs to cache this,
        // probably by invoking something on item service much like
        // getCachedPrivileges)
        if (!privs.contains(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)) {
          if (attachment != null && attachment.isRestricted()) {
            throw new AccessDeniedException(
                urlHelper.getString(
                    "viewitem.missingprivileges",
                    AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS));
          }
        }
      }
      String mimeType = mimeService.getMimeTypeForFilename(path);
      InitialFilestoreStream contentStream =
          new InitialFilestoreStream(itemId, fileHandle, path, mimeType);

      if (!contentStream.exists()) {
        response.sendError(404);
        return;
      }
      FilestoreContentStream filteredStream = contentStream;
      List<FilestoreContentFilter> filterList = fileFilterService.getFilters();
      for (FilestoreContentFilter filter : filterList) {
        filteredStream = filter.filter(filteredStream, request, response);
        if (filteredStream == null) {
          return;
        }
      }
      if (itemId != null) {
        auditor.audit(request, new ViewAuditEntry("file:" + mimeType, path), itemId, attachment);
      }
      contentStreamWriter.outputStream(request, response, filteredStream);
    } catch (AccessDeniedException ade) {
      if (CurrentUser.isGuest()) {
        LogonSection.forwardToLogon(
            controller,
            request,
            response,
            "file" + request.getPathInfo(),
            LogonSection.STANDARD_LOGON_PATH);
      } else {
        response.sendError(403);
      }
      return;
    }
  }

  private IAttachment getAttachmentWithDifferentPriv(ItemKey itemId, String path) {
    try {
      return new UnmodifiableAttachments(itemService.get(itemId).getAttachments())
          .getAttachmentByFilename(path);
    } catch (Exception e) {
      return new UnmodifiableAttachments(
              itemService.getItemWithViewAttachmentPriv(itemId).getAttachments())
          .getAttachmentByFilename(path);
    }
  }

  public class InitialFilestoreStream extends WrappedContentStream
      implements FilestoreContentStream {
    private final ItemKey itemId;
    private final FileHandle handle;
    private final String filepath;

    public InitialFilestoreStream(
        ItemKey itemId, FileHandle fileHandle, String path, String mimeType) {
      super(fileSystemService.getContentStream(fileHandle, path, mimeType));
      this.handle = fileHandle;
      this.filepath = path;
      this.itemId = itemId;
    }

    @Override
    public boolean useDefaultCacheControl() {
      return true;
    }

    @Override
    public String getFileDirectoryPath() {
      String path = getFilepath();
      int ind = path.lastIndexOf('/');
      if (ind == -1) {
        return ""; //$NON-NLS-1$
      }
      return path.substring(0, ind);
    }

    @Override
    public FileHandle getFileHandle() {
      return handle;
    }

    @Override
    public String getFilepath() {
      return filepath;
    }

    @Override
    public ItemKey getItemId() {
      return itemId;
    }
  }
}
