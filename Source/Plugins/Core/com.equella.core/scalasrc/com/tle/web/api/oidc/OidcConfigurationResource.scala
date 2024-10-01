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

package com.tle.web.api.oidc

import com.tle.common.beans.exception.NotFoundException
import com.tle.core.guice.Bind
import com.tle.integration.oidc.OidcSettingsPrivilegeTreeProvider
import com.tle.integration.oidc.idp.{
  GenericIdentityProviderDetails,
  IdentityProvider,
  IdentityProviderDetails,
  RoleConfiguration
}
import com.tle.integration.oidc.service.OidcConfigurationService
import com.tle.web.api.ApiErrorResponse.{badRequest, resourceNotFound, serverError}
import io.swagger.annotations.{Api, ApiOperation}

import java.net.URL
import javax.inject.{Inject, Singleton}
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, PUT, Path, Produces}

/**
  * Structure for the common details of an Identity Provider, which excludes
  * sensitive values like the client secret.
  */
case class CommonDetailsResponse(name: String,
                                 platform: String,
                                 authCodeClientId: String,
                                 authUrl: URL,
                                 keysetUrl: URL,
                                 tokenUrl: URL,
                                 usernameClaim: Option[String],
                                 defaultRoles: Set[String],
                                 roleConfig: Option[RoleConfiguration])

sealed trait IdentityProviderResponse {
  val commonDetails: CommonDetailsResponse
}

case class GenericIdentityProviderResponse(commonDetails: CommonDetailsResponse,
                                           apiUrl: URL,
                                           apiClientId: String)
    extends IdentityProviderResponse

object IdentityProviderResponse {
  def apply(idp: IdentityProviderDetails): Either[Throwable, IdentityProviderResponse] = {
    def commonDetails = CommonDetailsResponse(
      name = idp.commonDetails.name,
      platform = idp.commonDetails.platform.toString,
      authCodeClientId = idp.commonDetails.authCodeClientId,
      authUrl = idp.commonDetails.authUrl,
      keysetUrl = idp.commonDetails.keysetUrl,
      tokenUrl = idp.commonDetails.tokenUrl,
      usernameClaim = idp.commonDetails.usernameClaim,
      defaultRoles = idp.commonDetails.defaultRoles,
      roleConfig = idp.commonDetails.roleConfig
    )

    idp match {
      case generic: GenericIdentityProviderDetails =>
        Right(
          GenericIdentityProviderResponse(
            commonDetails = commonDetails,
            generic.apiUrl,
            generic.apiClientId
          ))
      case other =>
        Left(
          new RuntimeException(
            s"Found unsupported OIDC Identity Provider: ${other.commonDetails.platform}"))
    }
  }
}

@Bind
@Singleton
@Path("oidc/config")
@Produces(Array("application/json"))
@Api("OIDC configuration")
class OidcConfigurationResource {

  @Inject private var oidcConfigurationService: OidcConfigurationService = _
  @Inject private var aclProvider: OidcSettingsPrivilegeTreeProvider     = _

  private def errorHandler(error: Throwable): Response =
    error match {
      case e: NotFoundException        => resourceNotFound(e.getMessage)
      case e: IllegalArgumentException => badRequest(e.getMessage)
      case e                           => serverError(e.getMessage)
    }

  @GET
  @ApiOperation(
    value = "Retrieve OIDC configuration",
    response = classOf[IdentityProviderResponse],
  )
  def getConfiguration: Response = {
    aclProvider.checkAuthorised()

    oidcConfigurationService.get
      .flatMap(IdentityProviderResponse(_))
      .fold(
        errorHandler,
        Response.ok(_).build()
      )
  }

  @PUT
  @ApiOperation(
    value = "Save OIDC configuration",
    response = classOf[Unit],
  )
  def saveConfiguration(config: IdentityProvider): Response = {
    aclProvider.checkAuthorised()
    oidcConfigurationService.save(config).fold(errorHandler, _ => Response.ok().build())
  }
}
