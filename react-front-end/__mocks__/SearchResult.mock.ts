// @ts-nocheck
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
import { range } from "lodash";
import { v4 as uuidv4 } from "uuid";

export const getEmptySearchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> =
  {
    start: 0,
    length: 0,
    available: 0,
    results: [],
    highlight: [],
  };

export const getSearchResult: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> =
  {
    start: 0,
    length: 10,
    available: 12,
    results: [
      {
        uuid: "9b9bf5a9-c5af-490b-88fe-7e330679fad2",
        version: 1,
        name: "new title",
        status: "personal",
        createdDate: new Date("2014-06-11T10:28:58.190+10:00"),
        modifiedDate: new Date("2014-06-11T10:28:58.393+10:00"),
        collectionId: "6b356e2e-e6a0-235a-5730-15ad1d8ad630",
        commentCount: 0,
        attachments: [
          {
            attachmentType: "file",
            id: "29e0fe1b-dbd6-4c98-9e7a-d957d9c731f5",
            description: "B.txt",
            preview: false,
            mimeType: "text/plain",
            hasGeneratedThumb: true,
            links: {
              view: "http://localhost:8080/rest/items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/?attachment.uuid=29e0fe1b-dbd6-4c98-9e7a-d957d9c731f5",
              thumbnail: "./thumb.jpg",
            },
          },
        ],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/",
          self: "http://localhost:8080/rest/api/item/9b9bf5a9-c5af-490b-88fe-7e330679fad2/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc227c",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "2534e329-e37e-4851-896e-51d8b39104c4",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:27:14.800+10:00"),
        modifiedDate: new Date("2014-06-11T09:27:14.803+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/2534e329-e37e-4851-896e-51d8b39104c4/1/",
          self: "http://localhost:8080/rest/api/item/2534e329-e37e-4851-896e-51d8b39104c4/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "925f5dd2-66eb-4b68-85be-93837af785d0",
        version: 1,
        name: "new title",
        status: "personal",
        createdDate: new Date("2014-06-10T16:01:25.817+10:00"),
        modifiedDate: new Date("2014-06-10T16:01:25.967+10:00"),
        collectionId: "6b356e2e-e6a0-235a-5730-15ad1d8ad630",
        commentCount: 0,
        attachments: [
          {
            attachmentType: "file",
            id: "0a89415c-73b6-4e9b-8372-197b6ba4946c",
            description: "B.txt",
            preview: false,
            mimeType: "text/plain",
            hasGeneratedThumb: true,
            links: {
              view: "http://localhost:8080/rest/items/925f5dd2-66eb-4b68-85be-93837af785d0/1/?attachment.uuid=0a89415c-73b6-4e9b-8372-197b6ba4946c",
              thumbnail: "./thumb.jpg",
            },
          },
        ],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/925f5dd2-66eb-4b68-85be-93837af785d0/1/",
          self: "http://localhost:8080/rest/api/item/925f5dd2-66eb-4b68-85be-93837af785d0/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2271",
        name: "a",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2272",
        name: "b",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
        isLatestVersion: true,
        bookmarkId: 123,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2273",
        name: "c",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2274",
        name: "d",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2275",
        name: "e",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2276",
        name: "f",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2277",
        name: "g",
        version: 1,
        status: "live",
        createdDate: new Date("2014-06-11T09:31:08.557+10:00"),
        modifiedDate: new Date("2014-06-11T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
        isLatestVersion: true,
      },
      {
        uuid: "266bb0ff-a730-4658-aec0-c68bbefc2278",
        name: "last modified item",
        version: 1,
        status: "live",
        createdDate: new Date("2020-07-10T09:31:08.557+10:00"),
        modifiedDate: new Date("2020-07-10T09:31:08.557+10:00"),
        collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
        commentCount: 0,
        attachments: [],
        thumbnail: "initial",
        displayFields: [],
        keywordFoundInAttachment: false,
        links: {
          view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
          self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
        },
        isLatestVersion: true,
      },
    ],
    highlight: [],
  };

export const getSearchResultsCustom = (
  numberOfResults: number
): OEQ.Search.SearchResult<OEQ.Search.SearchResultItem> => ({
  start: 0,
  length: 10,
  available: numberOfResults,
  results: range(numberOfResults).map((i) => ({
    uuid: uuidv4(),
    name: `item ${i}`,
    version: 1,
    status: "live",
    createdDate: new Date("2020-07-10T09:31:08.557+10:00"),
    modifiedDate: new Date("2020-07-10T09:31:08.557+10:00"),
    collectionId: "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c",
    commentCount: 0,
    attachments: [],
    thumbnail: "initial",
    displayFields: [],
    keywordFoundInAttachment: false,
    links: {
      view: "http://localhost:8080/rest/items/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
      self: "http://localhost:8080/rest/api/item/266bb0ff-a730-4658-aec0-c68bbefc227c/1/",
    },
    isLatestVersion: true,
  })),
  highlight: [],
});
