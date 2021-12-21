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
import {
  IconButton,
  Snackbar,
  SnackbarContent,
  Theme,
} from "@material-ui/core";
import amber from "@material-ui/core/colors/amber";
import green from "@material-ui/core/colors/green";
import { makeStyles } from "@material-ui/core/styles";
import CheckCircleIcon from "@material-ui/icons/CheckCircle";
import CloseIcon from "@material-ui/icons/Close";
import ErrorIcon from "@material-ui/icons/Error";
import InfoIcon from "@material-ui/icons/Info";
import WarningIcon from "@material-ui/icons/Warning";
import * as React from "react";
import { commonString } from "../util/commonstrings";

const variantIcon = {
  success: CheckCircleIcon,
  warning: WarningIcon,
  error: ErrorIcon,
  info: InfoIcon,
};

const useStyles = makeStyles((theme: Theme) => ({
  success: {
    backgroundColor: green[600],
  },
  error: {
    backgroundColor: theme.palette.error.dark,
  },
  info: {
    backgroundColor: theme.palette.primary.dark,
  },
  warning: {
    backgroundColor: amber[700],
  },
  icon: {
    fontSize: 20,
  },
  iconVariant: {
    opacity: 0.9,
    marginRight: theme.spacing(1),
  },
  message: {
    display: "flex",
    alignItems: "center",
  },
}));

export type MessageInfoVariant = "success" | "warning" | "error" | "info";

export interface MessageInfoProps {
  open: boolean;
  onClose: () => void;
  title: string;
  variant: MessageInfoVariant;
}

const MessageInfo = ({ open, title, variant, onClose }: MessageInfoProps) => {
  const styles = useStyles();
  const Icon = variantIcon[variant];
  return (
    <Snackbar open={open} onClose={onClose} autoHideDuration={5000}>
      <SnackbarContent
        className={styles[variant]}
        aria-describedby="client-snackbar"
        message={
          <span id="client-snackbar" className={styles.message}>
            <Icon className={`${styles.icon} ${styles.iconVariant}`} />
            {title}
          </span>
        }
        action={
          <IconButton
            key="close"
            aria-label={commonString.action.close}
            color="inherit"
            onClick={onClose}
          >
            <CloseIcon className={styles.icon} />
          </IconButton>
        }
      />
    </Snackbar>
  );
};

export default MessageInfo;
