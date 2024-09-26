package io.github.openequella.rest

import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.{GetMethod, PutMethod}
import org.junit.Assert.assertEquals
import org.testng.annotations.Test

import java.util

class OidcConfigurationApiTest extends AbstractRestApiTest {
  private val OIDC_ENDPOINT = getTestConfig.getInstitutionUrl + "api/oidc/config"
  private val API_CLIENT_ID = "1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps"
  private val PLATFORM      = "GENERIC"
  private val AUTH_URL      = "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/authorize"

  private def buildRequestBody = {
    val body = mapper.createObjectNode()
    body.put("name", "Auth0")
    body.put("platform", PLATFORM)
    body.put("authCodeClientId", "C5tvBaB7svqjLPe0dDPBicgPcVPDJumZ")
    body.put("authCodeClientSecret",
             "_If_ItaRIw6eq0mKGMgoetTLjnGiuGvYbC012yA26F8I4vIZ7PaLGYwF3T89Yo1L")
    body.put("authUrl", AUTH_URL)
    body.put("keysetUrl", "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/.well-known/jwks.json")
    body.put("tokenUrl", "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/oauth/token")
    body.put("enabled", true)
    body.put("apiUrl", "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/api/v2/users")
    body.put("apiClientId", "1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps")
    body.put("apiClientSecret", "JKpZOuwluzwHnNXR-rxhhq_p4dWmMz-EhtRHjyfza5nCiG-J2SHrdeXAkyv2GB4I")

    val defaultRoles = body.putArray("defaultRoles")
    defaultRoles.add("admin")

    body
  }

  @Test(description = "Create a new OIDC configuration")
  def add(): Unit = {
    val request   = new PutMethod(OIDC_ENDPOINT)
    val resp_code = makeClientRequestWithEntity(request, buildRequestBody)
    assertEquals(HttpStatus.SC_OK, resp_code)
  }

  @Test(description = "Retrieve the OIDC configuration", dependsOnMethods = Array("add"))
  def get(): Unit = {
    val request = new GetMethod(OIDC_ENDPOINT)

    val resp_code = makeClientRequest(request)
    assertEquals(HttpStatus.SC_OK, resp_code)

    val config = mapper.readTree(request.getResponseBody())
    // Confirm the common values are returned.
    assertEquals(config.get("platform").asText(), PLATFORM)
    assertEquals(config.get("authUrl").asText(), AUTH_URL)
    // Confirm Platform-specific values are returned.
    assertEquals(config.get("apiClientId").asText(), API_CLIENT_ID)
  }

  @Test(description = "Return 400 when creating with invalid values")
  def invalidValues(): Unit = {
    val body = buildRequestBody
    body.put("authCodeClientId", "")
    body.put("keysetUrl", "http://abc/ keyset/")
    body.put("apiClientId", "")

    val request   = new PutMethod(OIDC_ENDPOINT)
    val resp_code = makeClientRequestWithEntity(request, body)
    assertEquals(HttpStatus.SC_BAD_REQUEST, resp_code)

    val result = mapper.readTree(request.getResponseBody())
    val errors = result.get("errors").findValue("message").asText()
    assertEquals(
      "Missing value for required field: Authorisation Code flow Client ID," +
        "Invalid value for Key set URL: Illegal character in path at index 11: http://abc/ keyset/," +
        "Missing value for required field: IdP API Client ID",
      errors
    )
  }

  @Test(description = "Return 400 when creating with unsupported platform")
  def unsupportedPlatforms(): Unit = {
    val body = buildRequestBody
    body.put("platform", "GitHub")

    val request   = new PutMethod(OIDC_ENDPOINT)
    val resp_code = makeClientRequestWithEntity(request, body)
    assertEquals(HttpStatus.SC_BAD_REQUEST, resp_code)
  }
}
