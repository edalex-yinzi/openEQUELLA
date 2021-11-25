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
import * as RA from "fp-ts/ReadonlyArray";
import { API_BASE_URL } from "../AppConfig";

/**
 * Lookup groups known in oEQ.
 *
 * @param ids An array of oEQ ids
 */
export const resolveGroups = async (
  ids: ReadonlyArray<string>
): Promise<OEQ.UserQuery.GroupDetails[]> =>
  (
    await OEQ.UserQuery.lookup(API_BASE_URL, {
      users: [],
      groups: RA.toArray<string>(ids),
      roles: [],
    })
  ).groups;
