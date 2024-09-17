package com.tle.integration.oidc.idp

import java.net.{URI, URL}

sealed trait IdentityProviderDetails

/**
  * The common details configured for SSO with an Identity Provider, but with
  * some slightly more concrete types (e.g. `URL`) compared to `IdentityProvider`
  * which needed to be looser for REST endpoints etc.
  *
  * @param name Name of the Identity Provider.
  * @param platform One of the supported Identity Provider: [[IdentityProviderPlatform]]
  * @param authCodeClientId ID of an OAuth2 client registered in the selected Identity Provider, used specifically in
  *                         the Authorization Code flow
  * @param authCodeClientSecret Secret key used with `authCodeClientId` specifically in the Authorization Code flow
  * @param authUrl The URL used to initiate the OAuth2 authorisation process
  * @param keysetUrl The URL used to initiate the OAuth2 authorisation process
  * @param tokenUrl The URL used to obtain an access token from the selected Identity Provider
  * @param usernameClaim Custom claim used to retrieve a meaningful username from an ID token
  * @param defaultRoles A list of default OEQ roles to assign to the user's session.
  * @param roleConfig Optional configuration for custom roles assigned to the user's session. If None, use the default roles.
  */
case class CommonDetails(name: String,
                         platform: IdentityProviderPlatform.Value,
                         authCodeClientId: String,
                         authCodeClientSecret: String,
                         authUrl: URL,
                         keysetUrl: URL,
                         tokenUrl: URL,
                         usernameClaim: Option[String],
                         defaultRoles: Set[String],
                         roleConfig: Option[RoleConfiguration])

/**
  * Configuration details for a generic Identity Provider. In addition to the common configuration for SSO,
  * the details of how to interact with the Identity Provider's APIs are also included.
  *
  * @param commonDetails Common details configured for SSO with a generic Identity Provider
  * @param apiUrl The API endpoint for the Identity Provider, use for operations such as search for users
  * @param apiClientId Client ID used to get an Authorisation Token to use with the Identity Provider's API
  *                    (for user searching etc)
  * @param apiClientSecret Client Secret used with `apiClientId` to get an Authorization Token to use with
  *                        the Identity Provider's API (for user searching etc)
  */
case class GenericIdentityProviderDetails(
    commonDetails: CommonDetails,
    apiUrl: URL,
    apiClientId: String,
    apiClientSecret: String,
) extends IdentityProviderDetails

object IdentityProviderDetails {

  private def commonDetails(idp: IdentityProvider): CommonDetails = CommonDetails(
    name = idp.name,
    platform = idp.platform,
    authCodeClientId = idp.authCodeClientId,
    authCodeClientSecret = idp.authCodeClientSecret,
    authUrl = URI.create(idp.authUrl).toURL,
    keysetUrl = URI.create(idp.keysetUrl).toURL,
    tokenUrl = URI.create(idp.tokenUrl).toURL,
    usernameClaim = idp.usernameClaim,
    defaultRoles = idp.defaultRoles,
    roleConfig = idp.roleConfig
  )

  def apply[T <: IdentityProvider](idp: T): IdentityProviderDetails =
    idp match {
      case generic: GenericIdentityProvider =>
        GenericIdentityProviderDetails(
          commonDetails = commonDetails(generic),
          apiUrl = URI.create(generic.apiUrl).toURL,
          apiClientId = generic.apiClientId,
          apiClientSecret = generic.apiClientSecret
        )
      case _ =>
        throw new IllegalArgumentException(s"Unsupported Identity Provider: ${idp.platform}")
    }
}
