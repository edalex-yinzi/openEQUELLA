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
import userEvent from "@testing-library/user-event";
import * as React from "react";
import { render, screen } from "@testing-library/react";
import {
  SearchPageBody,
  SearchPageBodyProps,
} from "../../../tsrc/search/SearchPageBody";
import "@testing-library/jest-dom/extend-expect";
import { defaultSearchPageRefinePanelConfig } from "../../../tsrc/search/SearchPageHelper";
import { queryCollectionSelector } from "./SearchPageTestHelper";

const defaultSearchPageBodyProps: SearchPageBodyProps = {
  pathname: "/page/search",
};

describe("<SearchPageBody />", () => {
  const renderSearchPageBody = (
    props: SearchPageBodyProps = defaultSearchPageBodyProps
  ) => {
    return render(<SearchPageBody {...props} />);
  };

  it("supports additional panels", () => {
    const label = "additional Panel";
    const { queryByLabelText } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      additionalPanels: [<div aria-label={label} />],
    });

    expect(queryByLabelText(label)).toBeInTheDocument();
  });

  it("supports additional headers", () => {
    const text = "additional button";
    const { queryByText } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      additionalPanels: [<button>{text}</button>],
    });

    expect(queryByText(text, { selector: "button" })).toBeInTheDocument();
  });

  it("controls the visibility of Refine search filters", () => {
    // Because each filter is controlled in the same way, we use CollectionSelector as the testing target.
    const { container } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      refinePanelConfig: {
        ...defaultSearchPageRefinePanelConfig,
        // Do not display CollectionSelector.
        enableCollectionSelector: false,
      },
    });

    expect(queryCollectionSelector(container)).not.toBeInTheDocument();
  });

  it("supports custom sorting options", () => {
    const option = "custom option";
    const { container } = renderSearchPageBody({
      ...defaultSearchPageBodyProps,
      headerConfig: {
        customSortingOptions: new Map([["RANK", option]]),
      },
    });

    const sortingDropdown = container.querySelector("#sort-order-select");
    if (!sortingDropdown) {
      throw new Error("Failed to find the Sorting selector");
    }
    userEvent.click(sortingDropdown);
    expect(screen.queryByText(option)).toBeInTheDocument();
  });
});