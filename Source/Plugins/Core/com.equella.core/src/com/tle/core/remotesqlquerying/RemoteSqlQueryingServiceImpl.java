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

package com.tle.core.remotesqlquerying;

import com.google.common.base.Throwables;
import com.tle.common.remotesqlquerying.RemoteRemoteSqlQueryingService;
import com.tle.core.guice.Bind;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

@Bind(RemoteRemoteSqlQueryingService.class)
@Singleton
@SuppressWarnings("nls")
public class RemoteSqlQueryingServiceImpl implements RemoteRemoteSqlQueryingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteSqlQueryingServiceImpl.class);

  @Override
  public void testConnection(String driverClass, String jdbcUrl, String username, String password) {
    try {
      HikariDataSource source = new HikariDataSource();
      source.setDriverClassName(driverClass);
      source.setJdbcUrl(jdbcUrl);
      source.setUsername(username);
      source.setPassword(password);
      source.setMaximumPoolSize(1);

      String testQuery =
          "SELECT COUNT(*) FROM "
              + (jdbcUrl.startsWith("jdbc:oracle") ? "ALL_TABLES" : "information_schema.tables");
      new JdbcTemplate(source).query(testQuery, IGNORE_ROWS);

      source.close();
    } catch (Exception ex) {
      LOGGER.warn("Test connection failure", ex);
      Throwable rootCause = Throwables.getRootCause(ex);
      throw new RuntimeException(
          "Error attempting to connect or while executing test query: " + rootCause.getMessage());
    }
  }

  private static final RowCallbackHandler IGNORE_ROWS =
      new RowCallbackHandler() {
        @Override
        public void processRow(ResultSet rs) throws SQLException {
          // Do nothing!
        }
      };
}
