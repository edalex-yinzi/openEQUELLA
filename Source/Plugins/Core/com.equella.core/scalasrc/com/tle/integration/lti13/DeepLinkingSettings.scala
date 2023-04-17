package com.tle.integration.lti13

import cats.implicits._
import com.auth0.jwt.interfaces.DecodedJWT
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.parser.decode

/**
  * Data structure for LTI 1.3 deep linking settings.
  *
  * @param deepLinkReturnUrl URL where the tool redirects the user back to the platform.
  * @param acceptTypes An list of resource types accepted.
  * @param acceptPresentationDocumentTargets A list of supported document targets (e.g. iframe & window).
  * @param acceptMediaTypes A list of accepted media types. Only applies to File types.
  * @param acceptMultiple Whether selecting multiple resources in a single response is allowed.
  * @param acceptLineItem Whether the platform supports line items.
  * @param autoCreate Whether to persist the selected resource in the platform.
  * @param title Default text for the selected resource.
  * @param text Default description for the selected resources.
  * @param data An opaque value which must be included in the response if it's present in the request.
  */
case class DeepLinkingSettings(
    deepLinkReturnUrl: String,
    acceptTypes: Array[String],
    acceptPresentationDocumentTargets: Array[String],
    acceptMediaTypes: Option[String],
    acceptMultiple: Option[Boolean],
    acceptLineItem: Option[Boolean],
    autoCreate: Option[Boolean],
    title: Option[String],
    text: Option[String],
    data: Option[String]
)

object DeepLinkingSettings {
  implicit val config: Configuration                 = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[DeepLinkingSettings] = deriveConfiguredDecoder

  private def decodeDeepLinkingSettings(settings: String): Either[InvalidJWT, DeepLinkingSettings] =
    decode[DeepLinkingSettings](settings)
      .leftMap(error => InvalidJWT(s"Failed to decode deep linking settings: ${error.getMessage}"))

  def apply(jwt: DecodedJWT): Either[InvalidJWT, DeepLinkingSettings] = {
    getClaimStringRepr(jwt, Lti13Claims.DEEP_LINKING_SETTINGS)
      .toRight(InvalidJWT("Deep linking settings are missing in the token"))
      .flatMap(decodeDeepLinkingSettings)
  }
}
