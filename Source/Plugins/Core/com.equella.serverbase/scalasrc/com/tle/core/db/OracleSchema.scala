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

package com.tle.core.db

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.types.{DbUUID, InstId, String255}
import io.doolse.simpledba.Iso
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.jdbc.oracle._
import shapeless.{::, Generic, HNil}
import io.doolse.simpledba.syntax._

object OracleSchema extends DBSchemaMigration with DBSchema with DBQueries with StdOracleColumns {
  // General note on the queries in this class - With the advent of hibernate 5,
  // queries with '?' in them need to be ordinal ( ie `?4` ).  However, this class
  // does not leverage the JPA / Hibernate logic, so we can leave the `?`s as-is.

  implicit lazy val config = {
    val escaped = StandardJDBC.escapeReserved(oracleReserved + "key") _
    setupLogging(oracleConfig.copy(escapeColumnName = escaped))
  }

  lazy val hibSeq = Sequence[Long]("hibernate_sequence")

  def autoIdCol = longCol

  def dbUuidCol =
    wrap[String, DbUUID](stringCol,
                         _.isoMap(Iso(_.id.toString, DbUUID.fromString)),
                         _.copy(typeName = "VARCHAR(36)"))
}
