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
import * as OEQ from "@openequella/rest-api-client";
import { Meta, StoryFn } from "@storybook/react";
import * as React from "react";
import {
  keyResources,
  topicWithChildren,
  topicWithHtmlDesc,
} from "../../__mocks__/Hierarchy.mock";
import HierarchyPanel, {
  HierarchyPanelProps,
} from "../../tsrc/hierarchy/components/HierarchyPanel";

export default {
  title: "Hierarchy/HierarchyPanel",
  component: HierarchyPanel,
} as Meta<HierarchyPanelProps>;

const standardHierarchy: OEQ.BrowseHierarchy.HierarchyTopic<OEQ.BrowseHierarchy.KeyResource> =
  {
    summary: topicWithChildren,
    keyResources: keyResources,
    parents: [
      { name: "Parent1", compoundUuid: "uuid1" },
      { name: "Parent2", compoundUuid: "uuid2" },
    ],
  };

const hierarchyWithHtmlContent: OEQ.BrowseHierarchy.HierarchyTopic<OEQ.BrowseHierarchy.KeyResource> =
  {
    ...standardHierarchy,
    summary: topicWithHtmlDesc,
  };

export const Standard: StoryFn<HierarchyPanelProps> = (args) => (
  <HierarchyPanel {...args} />
);
Standard.args = {
  hierarchy: standardHierarchy,
};

export const HierarchyWithHtmlContent: StoryFn<HierarchyPanelProps> = (
  args,
) => <HierarchyPanel {...args} />;
HierarchyWithHtmlContent.args = {
  hierarchy: hierarchyWithHtmlContent,
};
