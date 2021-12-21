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
import { languageStrings } from "../util/langstrings";
import * as OEQ from "@openequella/rest-api-client";

interface SettingCategory {
  name: string;
  desc: string;
}

export interface SettingGroup {
  category: SettingCategory;
  settings: OEQ.Settings.GeneralSetting[];
}

/**
 * Group all settings by their category and sort each group by setting name
 * @param {GeneralSetting[]} settings
 * @returns SettingGroup[] A array of SettingGroup which includes a category and settings of the category
 */
export const groupMap = (
  settings: OEQ.Settings.GeneralSetting[]
): SettingGroup[] => {
  const settingCategories: { [key: string]: SettingCategory } =
    languageStrings.settings;

  return Object.keys(settingCategories).map((key) => {
    const settingsOfCategory = settings
      .filter((setting) => setting.group === key)
      .sort((s1, s2) => {
        return s1.name > s2.name ? 1 : -1;
      });
    return { category: settingCategories[key], settings: settingsOfCategory };
  });
};
