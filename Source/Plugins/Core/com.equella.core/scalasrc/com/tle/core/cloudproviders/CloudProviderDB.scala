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

package com.tle.core.cloudproviders

import java.util.concurrent.TimeUnit
import java.util.{Locale, UUID}
import cats.data.{OptionT, ValidatedNec}
import cats.effect.IO
import cats.syntax.apply._
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.tle.core.db._
import com.tle.core.db.dao.{EntityDB, EntityDBExt}
import com.tle.core.db.tables.OEQEntity
import com.tle.core.validation.{EntityStdEdits, EntityValidation}
import com.tle.legacy.LegacyGuice
import fs2.Stream
import io.circe.generic.semiauto._
import io.doolse.simpledba.Iso
import io.doolse.simpledba.circe.circeJsonUnsafe
import org.slf4j.LoggerFactory

case class CloudProviderData(baseUrl: String,
                             iconUrl: Option[String],
                             vendorId: String,
                             providerAuth: CloudOAuthCredentials,
                             oeqAuth: CloudOAuthCredentials,
                             serviceUrls: Map[String, ServiceUrl],
                             viewers: Map[String, Map[String, Viewer]])

object CloudProviderData {

  implicit val decoderV = deriveDecoder[Viewer]
  implicit val encoderV = deriveEncoder[Viewer]
  implicit val decoderS = deriveDecoder[ServiceUrl]
  implicit val encoderS = deriveEncoder[ServiceUrl]
  implicit val decoderC = deriveDecoder[CloudOAuthCredentials]
  implicit val encoderC = deriveEncoder[CloudOAuthCredentials]

  implicit val decoder = deriveDecoder[CloudProviderData]
  implicit val encoder = deriveEncoder[CloudProviderData]
}

case class CloudProviderDB(entity: OEQEntity, data: CloudProviderData)

object CloudProviderDB {

  val FieldVendorId    = "vendorId"
  val RefreshServiceId = "refresh"
  val Logger           = LoggerFactory.getLogger(getClass)

  val tokenCache =
    LegacyGuice.replicatedCacheService.getCache[String]("cloudRegTokens", 100, 1, TimeUnit.HOURS)

  type CloudProviderVal[A] = ValidatedNec[EntityValidation, A]

  implicit val dbExt: EntityDBExt[CloudProviderDB] =
    new EntityDBExt[CloudProviderDB] {
      val dataIso = circeJsonUnsafe[CloudProviderData]
      val iso = Iso(
        oeq => CloudProviderDB(oeq, dataIso.from(oeq.data)),
        scdb => scdb.entity.copy(data = dataIso.to(scdb.data))
      )

      override def typeId: String = "cloudprovider"
    }

  def toInstance(db: CloudProviderDB): CloudProviderInstance = {
    val oeq  = db.entity
    val data = db.data
    CloudProviderInstance(
      id = oeq.uuid.id,
      name = oeq.name,
      description = oeq.description,
      vendorId = data.vendorId,
      baseUrl = data.baseUrl,
      iconUrl = data.iconUrl,
      providerAuth = data.providerAuth,
      oeqAuth = data.oeqAuth,
      serviceUrls = data.serviceUrls,
      viewers = data.viewers
    )
  }

  def validateRegistrationFields(oeq: OEQEntity,
                                 reg: CloudProviderRegistration,
                                 oeqAuth: CloudOAuthCredentials,
                                 locale: Locale): CloudProviderVal[CloudProviderDB] = {
    EntityValidation.nonBlankString(FieldVendorId, reg.vendorId) *>
      EntityValidation
        .standardValidation(EntityStdEdits(name = reg.name, description = reg.description), locale)
        .map { _ =>
          val data = CloudProviderData(
            baseUrl = reg.baseUrl,
            iconUrl = reg.iconUrl,
            vendorId = reg.vendorId,
            providerAuth = reg.providerAuth,
            oeqAuth = oeqAuth,
            serviceUrls = reg.serviceUrls,
            viewers = reg.viewers
          )
          CloudProviderDB(oeq, data)
        }
  }

  private def doEdit(
      oeq: CloudProviderDB,
      registration: CloudProviderRegistration): DB[CloudProviderVal[CloudProviderInstance]] =
    for {
      locale <- getContext.map(_.locale)
      validated = validateRegistrationFields(oeq.entity, registration, oeq.data.oeqAuth, locale)
      _ <- validated.traverse(cdb => flushDB(EntityDB.update[CloudProviderDB](oeq.entity, cdb)))
    } yield validated.map(toInstance)

  def refreshRegistration(id: UUID): OptionT[DB, CloudProviderVal[CloudProviderInstance]] =
    for {
      oeqProvider <- EntityDB.readOne(id)
      provider = toInstance(oeqProvider)
      refreshService <- OptionT.fromOption[DB](provider.serviceUrls.get(RefreshServiceId))
      validated <- OptionT {
        CloudProviderService
          .serviceRequest(refreshService,
                          provider,
                          Map.empty,
                          uri =>
                            sttp
                              .post(uri)
                              .body(CloudProviderRefreshRequest(id))
                              .response(asJson[CloudProviderRegistration]))
          .flatMap { response =>
            response.body match {
              case Right(Right(registration)) => doEdit(oeqProvider, registration).map(Option(_))
              case err =>
                dbLiftIO.liftIO(IO {
                  Logger.warn(s"Failed to refresh provider - $err")
                  Option.empty[CloudProviderVal[CloudProviderInstance]]
                })
            }
          }
      }
    } yield validated

  val readAll: Stream[DB, CloudProviderInstance] = {
    EntityDB.readAll[CloudProviderDB].map(toInstance)
  }

  def get(id: UUID): OptionT[DB, CloudProviderInstance] = {
    EntityDB.readOne(id).map(toInstance)
  }

  def getStream(id: UUID): Stream[DB, CloudProviderInstance] = {
    EntityDB.readOneStream(id).map(toInstance)
  }
}
