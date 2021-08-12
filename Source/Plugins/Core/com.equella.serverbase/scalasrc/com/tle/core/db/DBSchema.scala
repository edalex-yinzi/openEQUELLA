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

import java.util

import com.tle.core.db.migration.DBSchemaMigration
import com.tle.core.db.tables._
import com.tle.core.db.types.{DbUUID, InstId, JsonColumn, String255}
import com.tle.core.hibernate.factory.guice.HibernateFactoryModule
import fs2.Stream
import io.circe.{Json, JsonObject}
import io.doolse.simpledba._
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._
import shapeless._

import scala.collection.JavaConverters._
import scala.collection.mutable

trait DBSchema extends StdColumns {
  // General note on the queries in this class - With the advent of hibernate 5,
  // queries with '?' in them need to be ordinal ( ie `?4` ).  However, this class
  // does not leverage the JPA / Hibernate logic, so we can leave the `?`s as-is.

  implicit def config: JDBCConfig.Aux[C]

  implicit def dbUuidCol: C[DbUUID]

  def schemaSQL: JDBCSchemaSQL = config.schemaSQL

  val allTables: mutable.Buffer[TableDefinition]         = mutable.Buffer()
  val allIndexes: mutable.Buffer[(TableColumns, String)] = mutable.Buffer()

  def indexEach(cols: TableColumns, name: NamedColumn => String): Seq[(TableColumns, String)] =
    cols.columns.map { cb =>
      TableColumns(cols.name, Seq(cb)) -> name(cb)
    }

  implicit def dbJsonCol(implicit scol: C[String]): C[Json] =
    wrap[String, Json](scol, _.isoMap(JsonColumn.jsonStringIso), jsonColumnMod)

  def jsonColumnMod(ct: ColumnType): ColumnType = ct

  implicit def jsonColumns[A <: JsonColumn](
      implicit c: Iso[A, Option[String]],
      col: C[Option[String]]
  ): C[A] =
    wrap[Option[String], A](col, _.isoMap[A](c), jsonColumnMod)

  def autoIdCol: C[Long]

  val userAndInst   = Cols('user_id, 'institution_id)
  val itemViewId    = Cols('inst, 'item_uuid, 'item_version)
  val itemViewCount = TableMapper[ItemViewCount].table("viewcount_item").keys(itemViewId)
  val attachmentViewCount = TableMapper[AttachmentViewCount]
    .table("viewcount_attachment")
    .keys(itemViewId ++ Cols('attachment))

  val viewCountTables = Seq(itemViewCount.definition, attachmentViewCount.definition)

  allTables ++= viewCountTables

  val countByCol = JDBCQueries.queryRawSQL(
    "select sum(\"count\") from viewcount_item vci " +
      "inner join item i on vci.item_uuid = i.uuid and vci.item_version = i.version " +
      "inner join base_entity be on be.id = i.item_definition_id where be.id = ?",
    config.record[Long :: HNil],
    config.record[Option[Int] :: HNil]
  )

  val attachmentViewCountByCol = JDBCQueries.queryRawSQL(
    "select sum(\"count\") from viewcount_attachment vca " +
      "inner join attachment a on vca.attachment = a.uuid " +
      "inner join item i on a.item_id = i.id " +
      "inner join base_entity be on be.id = i.item_definition_id where be.id = ?",
    config.record[Long :: HNil],
    config.record[Option[Int] :: HNil]
  )

  val viewCountQueries = {
    val del1 = itemViewCount.delete.where(itemViewId, BinOp.EQ).build[(InstId, DbUUID, Int)]
    val del2 = attachmentViewCount.delete.where(itemViewId, BinOp.EQ).build[(InstId, DbUUID, Int)]
    ViewCountQueries(
      itemViewCount.writes,
      attachmentViewCount.writes,
      itemViewCount.byPK,
      itemViewCount.query.where(Cols('inst), BinOp.EQ).build,
      attachmentViewCount.byPK,
      attachmentViewCount.query.where(Cols('inst, 'item_uuid, 'item_version), BinOp.EQ).build,
      countByCol.as[Long => Stream[JDBCIO, Option[Int]]].andThen(_.map(_.getOrElse(0))),
      attachmentViewCountByCol
        .as[Long => Stream[JDBCIO, Option[Int]]]
        .andThen(_.map(_.getOrElse(0))),
      id => del1(id) ++ del2(id)
    )
  }

  val settingsRel =
    TableMapper[Setting].table("configuration_property").keys(Cols('institution_id, 'property))

  val settingsQueries = SettingsQueries(
    settingsRel.writes,
    settingsRel.byPK,
    settingsRel.query
      .where(Cols('institution_id), BinOp.EQ)
      .where(Cols('property), BinOp.LIKE)
      .build,
    settingsRel.query
      .where(Cols('property), BinOp.LIKE)
      .build
  )

  val entityTable = TableMapper[OEQEntity].table("entities").keys(Cols('inst_id, 'uuid))

  val entityTypeIdx = (entityTable.subset(Cols('inst_id, 'typeid)), "entityTypeIdx")

  val newEntityTables  = Seq(entityTable.definition)
  val newEntityIndexes = Seq(entityTypeIdx)

  val entityQueries = EntityQueries(
    entityTable.writes,
    entityTable.query.where(Cols('inst_id, 'typeid), BinOp.EQ).build,
    entityTable.byPK,
    entityTable.query.where(Cols('inst_id), BinOp.EQ).build
  )

  allTables ++= newEntityTables
  allIndexes ++= newEntityIndexes

  def creationSQL: util.Collection[String] = {
    allTables.map(schemaSQL.createTable) ++
      allIndexes.map(i => schemaSQL.createIndex(i._1, i._2))
  }.asJava
}

object DBSchema {
  lazy private val schemaForDBType: DBSchema with DBQueries with DBSchemaMigration = {
    val p = new HibernateFactoryModule
    p.getProperty("hibernate.connection.driver_class") match {
      case "org.postgresql.Driver"                        => PostgresSchema
      case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerSchema
      case "oracle.jdbc.driver.OracleDriver"              => OracleSchema
    }
  }

  def schema: DBSchema = schemaForDBType

  def schemaMigration: DBSchemaMigration = schemaForDBType

  def queries: DBQueries = schemaForDBType
}
