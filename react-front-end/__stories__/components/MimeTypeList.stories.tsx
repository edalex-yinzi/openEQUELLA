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
import * as React from "react";
import type { Meta, Story } from "@storybook/react";
import MimeTypeList, {
  MimeTypeFilterListProps,
} from "../../tsrc/settings/Search/searchfilter/MimeTypeList";
import * as OEQ from "@openequella/rest-api-client";

export default {
  title: "MimeTypeList",
  component: MimeTypeList,
  argTypes: {
    onChange: { action: "onChange" },
  },
} as Meta<MimeTypeFilterListProps>;

const defaultMimeTypes: OEQ.MimeType.MimeTypeEntry[] = [
  { mimeType: "image/png", desc: "This is a Image filter" },
  { mimeType: "image/jpeg", desc: "This is a Image filter" },
];

export const listOfMimeTypes: Story<MimeTypeFilterListProps> = (args) => (
  <MimeTypeList {...args} />
);
listOfMimeTypes.args = {
  entries: defaultMimeTypes,
  selected: ["image/png", "image/jpeg"],
};
