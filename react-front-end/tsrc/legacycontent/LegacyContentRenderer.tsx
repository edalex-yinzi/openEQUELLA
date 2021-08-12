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
import { makeStyles } from "@material-ui/core";
import * as React from "react";
import HtmlParser from "react-html-parser";
import JQueryDiv from "./JQueryDiv";
import { PageContent } from "./LegacyContent";
import { LegacyForm } from "./LegacyForm";

const useStyles = makeStyles((t) => ({
  noPadding: {
    padding: 0,
  },
}));

export function LegacyContentRenderer({
  afterHtml,
  fullscreenMode,
  html: { body, crumbs, form, upperbody },
  menuMode,
  noForm,
  script,
  state,
}: PageContent) {
  const classes = useStyles();

  // Effect responsible for the execution of the legacy scripts etc which were historically
  // added at the end of the server-side rendered HTML.
  React.useEffect(() => {
    // eslint-disable-next-line no-eval
    if (script) window.eval(script);
  }, [script]);

  React.useEffect(() => {
    if (afterHtml) afterHtml();
  }, [afterHtml]);

  const extraClass =
    !fullscreenMode && menuMode !== "HIDDEN" ? classes.noPadding : "";

  const mainContent = (
    <div className={`content ${extraClass}`}>
      {crumbs && <JQueryDiv id="breadcrumbs" html={crumbs} />}
      {upperbody && <JQueryDiv html={upperbody} />}
      <JQueryDiv html={body} />
    </div>
  );

  return noForm ? (
    mainContent
  ) : (
    <>
      <LegacyForm state={state}>{mainContent}</LegacyForm>
      {form && HtmlParser(form)}
    </>
  );
}
