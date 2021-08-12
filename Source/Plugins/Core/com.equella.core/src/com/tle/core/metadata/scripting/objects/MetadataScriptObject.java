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

package com.tle.core.metadata.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.FileHandleScriptType;
import com.tle.core.metadata.scripting.types.MetadataScriptType;

/** Referenced by the 'metadata' variable in a script */
@SuppressWarnings("nls")
public interface MetadataScriptObject extends ScriptObject {
  String DEFAULT_VARIABLE = "metadata";

  /**
   * Returns a {@link MetadataScriptType} object for the file provided. If the file does not exist
   * or cannot be read an empty object will be returned.
   *
   * @param f A handle to a file to retrieve the metadata of - e.g. image, video, etc.
   * @return the metadata for the file
   */
  MetadataScriptType getMetadata(FileHandleScriptType f);

  /**
   * Returns a {@link MetadataScriptType} object for the attachment provided. If the attachment type
   * is unsupported an empty object will be returned
   *
   * @param a A reference to an attachment to retrieve the metadata of
   * @return the metadata for the attachment
   */
  MetadataScriptType getMetadata(AttachmentScriptType a);
}
