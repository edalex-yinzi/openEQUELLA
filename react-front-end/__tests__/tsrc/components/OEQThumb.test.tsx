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
import { queryByLabelText, render } from "@testing-library/react";
import * as React from "react";
import "@testing-library/jest-dom/extend-expect";
import {
  brokenFileAttachment,
  equellaItemAttachment,
  fileAttachment,
  htmlAttachment,
  linkAttachment,
  resourceHtmlAttachment,
  resourceLinkAttachment,
} from "../../../__mocks__/OEQThumb.mock";
import OEQThumb from "../../../tsrc/components/OEQThumb";
import * as OEQ from "@openequella/rest-api-client";
import { languageStrings } from "../../../tsrc/util/langstrings";

describe("<OEQThumb/>", () => {
  const thumbLabels = languageStrings.searchpage.thumbnails;
  const buildOEQThumb = (
    attachment: OEQ.Search.Attachment,
    showPlaceHolder: boolean
  ) =>
    render(
      <OEQThumb attachment={attachment} showPlaceholder={showPlaceHolder} />
    );

  it.each<[string, OEQ.Search.Attachment, boolean, string]>([
    [
      "shows the placeholder icon when showPlaceholder is true",
      fileAttachment,
      true,
      thumbLabels.placeholder,
    ],
    [
      "shows thumbnail image when showPlaceholder is false",
      fileAttachment,
      false,
      thumbLabels.provided,
    ],
    [
      "shows default file thumbnail when brokenAttachment is true",
      brokenFileAttachment,
      false,
      thumbLabels.file,
    ],
    [
      "shows link icon thumbnail for a link attachment",
      linkAttachment,
      false,
      thumbLabels.link,
    ],
    [
      "shows link icon thumbnail for a resource link attachment",
      resourceLinkAttachment,
      false,
      thumbLabels.link,
    ],
    [
      "shows equella item thumbnail for a resource attachment pointing at an item summary",
      equellaItemAttachment,
      false,
      thumbLabels.item,
    ],
    [
      "shows html thumbnail for a web page attachment",
      htmlAttachment,
      false,
      thumbLabels.html,
    ],
    [
      "shows html thumbnail for a resource web page attachment",
      resourceHtmlAttachment,
      false,
      thumbLabels.html,
    ],
  ])(
    "%s",
    (
      _: string,
      attachment: OEQ.Search.Attachment,
      showPlaceHolder: boolean,
      query: string
    ) => {
      const { container } = buildOEQThumb(attachment, showPlaceHolder);
      expect(queryByLabelText(container, query)).toBeInTheDocument();
    }
  );
});
