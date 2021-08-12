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
import { Location } from "history";
import * as React from "react";
import { useEffect, useState } from "react";
import { Prompt, useHistory } from "react-router";
import { commonString } from "../util/commonstrings";
import { languageStrings } from "../util/langstrings";
import ConfirmDialog from "./ConfirmDialog";

/**
 * Prevent navigations triggered by Browsers' behaviours when this's required.
 */
export const blockNavigationWithBrowser = (preventNavigation: boolean) => {
  window.onbeforeunload = () => {
    if (preventNavigation) {
      return true;
    }
    return;
  };
};

export interface NavigationGuardProps {
  /**
   * Show a dialog if true, when route is going to change.
   */
  when: boolean;
}

/**
 * Use 'Prompt' provided by react-router to prevent navigation and
 * show 'ConfirmDialog'.
 * Also prevent navigations triggered by Browsers' behaviours.
 */
export const NavigationGuard = ({ when }: NavigationGuardProps) => {
  const history = useHistory();
  const { message, title } = languageStrings.navigationguard;

  const [showPrompt, setShowPrompt] = useState(false);
  const [confirmedNavigation, setConfirmedNavigation] = useState(false);
  const [navigateTo, setNavigateTo] = useState<Location>();

  /**
   * Navigate to other pages, depending on if user confirms and next location.
   */
  useEffect(() => {
    if (confirmedNavigation && navigateTo) {
      history.push(navigateTo.pathname);
    }
  }, [confirmedNavigation, navigateTo, history]);

  /**
   * Handle 'beforeunload' event when preventing navigation is required.
   * Do not handle this event when this component will unmount.
   */
  useEffect(() => {
    blockNavigationWithBrowser(when);
    return () => {
      window.onbeforeunload = null;
    };
  }, [when]);

  /**
   * Show a dialog to let user confirm and save the next location.
   * If user confirms then don't block the navigation and hide the dialog.
   */
  const blockNavigation = (location: Location): boolean => {
    if (confirmedNavigation) {
      return true;
    }
    setNavigateTo(location);
    setShowPrompt(true);
    return false;
  };

  const confirmNavigation = () => {
    setConfirmedNavigation(true);
    setShowPrompt(false);
  };

  const cancelNavigation = () => {
    setShowPrompt(false);
  };

  return (
    <>
      <Prompt when={when} message={blockNavigation} />
      <ConfirmDialog
        title={title}
        onConfirm={confirmNavigation}
        onCancel={cancelNavigation}
        open={showPrompt}
        confirmButtonText={commonString.action.ok}
      >
        {message}
      </ConfirmDialog>
    </>
  );
};
