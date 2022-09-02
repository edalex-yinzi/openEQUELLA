package com.tle.webtests.test.webservices.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import java.util.List;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

public class SearchSettingApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "SearchSettingApiTestClient";
  private static final String API_SEARCH_SETTINGS_PATH = "api/settings/search";
  private static final String API_SEARCH_FILTER_PATH = "api/settings/search/filter";

  private static final String DEFAULT_SORT_ORDER = "defaultSearchSort";
  private static final String SHOW_NON_LIVE = "searchingShowNonLiveCheckbox";
  private static final String AUTHENTICATE_FEEDS = "authenticateFeedsByDefault";

  private static final String DISABLE_OWNER_FILTER = "searchingDisableOwnerFilter";
  private static final String DISABLE_DATE_MODIFIED_FILTER = "searchingDisableDateModifiedFilter";
  private static final String DISABLE_IMAGE = "searchingDisableGallery";
  private static final String DISABLE_VIDEO = "searchingDisableVideos";
  private static final String DISABLE_FILE_COUNT = "fileCountDisabled";

  private static final String TITLE_BOOST = "titleBoost";
  private static final String DESCRIPTION_BOOST = "descriptionBoost";
  private static final String ATTACHMENT_BOOST = "attachmentBoost";

  private static final String URL_LEVEL = "urlLevel";

  private static final String DISABLE_CLOUD = "disabled";

  private final String FILTER_NAME = "name";
  private final String FILTER_ID = "id";
  private final String FILTER_MIME_TYPES = "mimeTypes";

  private final String IMAGE_FILTER = "image filter";
  private final String JPEG = "image/jpeg";
  private final String PNG = "image/png";

  private final String PDF_FILTER = "PDF filter";
  private final String PDF = "application/pdf";

  private final String BAD_FILTER_ID = "d43a4d03-73dc-48d0-8563-c6f4dc845bbb";
  private final String BAD_MIME_TYPE = "image/bad";

  private String newFilterId = "";
  private String secondeNewFilterId = "";

  private String RESPONSE_STATUS = "status";
  private String RESPONSE_MESSAGE = "message";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testSearchSettings() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    final String uri = PathUtils.urlPath(context.getBaseUrl(), API_SEARCH_SETTINGS_PATH);

    // Load and check initial settings
    final JsonNode initialSearchSettings = getEntity(uri, token);

    assertNull(initialSearchSettings.get(DEFAULT_SORT_ORDER));
    assertFalse(initialSearchSettings.get(SHOW_NON_LIVE).asBoolean());
    assertFalse(initialSearchSettings.get(AUTHENTICATE_FEEDS).asBoolean());

    assertFalse(initialSearchSettings.get(DISABLE_IMAGE).asBoolean());
    assertFalse(initialSearchSettings.get(DISABLE_VIDEO).asBoolean());
    assertFalse(initialSearchSettings.get(DISABLE_FILE_COUNT).asBoolean());
    assertFalse(initialSearchSettings.get(DISABLE_OWNER_FILTER).asBoolean());
    assertFalse(initialSearchSettings.get(DISABLE_DATE_MODIFIED_FILTER).asBoolean());

    assertEquals(initialSearchSettings.get(TITLE_BOOST).asInt(), 5);
    assertEquals(initialSearchSettings.get(DESCRIPTION_BOOST).asInt(), 3);
    assertEquals(initialSearchSettings.get(ATTACHMENT_BOOST).asInt(), 2);

    assertEquals(initialSearchSettings.get(URL_LEVEL).asInt(), 0);

    // Update settings
    final ObjectNode newSearchSettings = mapper.createObjectNode();

    newSearchSettings.put(DEFAULT_SORT_ORDER, "RATING");
    newSearchSettings.put(SHOW_NON_LIVE, true);
    newSearchSettings.put(AUTHENTICATE_FEEDS, true);

    newSearchSettings.put(DISABLE_IMAGE, true);
    newSearchSettings.put(DISABLE_VIDEO, true);
    newSearchSettings.put(DISABLE_FILE_COUNT, true);
    newSearchSettings.put(DISABLE_OWNER_FILTER, true);
    newSearchSettings.put(DISABLE_DATE_MODIFIED_FILTER, true);

    newSearchSettings.put(TITLE_BOOST, 4);
    newSearchSettings.put(DESCRIPTION_BOOST, 4);
    newSearchSettings.put(ATTACHMENT_BOOST, 4);

    newSearchSettings.put(URL_LEVEL, 1);

    HttpResponse response = putEntity(newSearchSettings.toString(), uri, token, true);
    assertEquals(response.getStatusLine().getStatusCode(), 204);

    // Load and check the updated settings
    final JsonNode updatedSearchSettings = getEntity(uri, token);

    assertEquals(updatedSearchSettings.get(DEFAULT_SORT_ORDER).asText(), "rating");
    assertTrue(updatedSearchSettings.get(SHOW_NON_LIVE).asBoolean());
    assertTrue(updatedSearchSettings.get(AUTHENTICATE_FEEDS).asBoolean());

    assertTrue(updatedSearchSettings.get(DISABLE_IMAGE).asBoolean());
    assertTrue(updatedSearchSettings.get(DISABLE_VIDEO).asBoolean());
    assertTrue(updatedSearchSettings.get(DISABLE_FILE_COUNT).asBoolean());
    assertTrue(updatedSearchSettings.get(DISABLE_OWNER_FILTER).asBoolean());
    assertTrue(updatedSearchSettings.get(DISABLE_DATE_MODIFIED_FILTER).asBoolean());

    assertEquals(updatedSearchSettings.get(TITLE_BOOST).asInt(), 4);
    assertEquals(updatedSearchSettings.get(DESCRIPTION_BOOST).asInt(), 4);
    assertEquals(updatedSearchSettings.get(ATTACHMENT_BOOST).asInt(), 4);

    assertEquals(updatedSearchSettings.get(URL_LEVEL).asInt(), 1);
  }

  @Test(dependsOnMethods = "testSearchSettings")
  public void testCreateSearchFilter() throws Exception {
    final String uri = searchFilterUri("");

    ObjectNode jsonBody = mapper.createObjectNode();
    jsonBody.put(FILTER_NAME, IMAGE_FILTER);
    ArrayNode mimeTypes = jsonBody.putArray(FILTER_MIME_TYPES);
    mimeTypes.add(JPEG);
    mimeTypes.add(PNG);
    // Create a search filter
    HttpResponse response = postEntity(jsonBody.toString(), uri, getToken(), false);
    assertEquals(response.getStatusLine().getStatusCode(), 201);
    JsonNode node = mapper.readTree(response.getEntity().getContent());
    newFilterId = node.get(FILTER_ID).asText();
    assertNotNull(newFilterId);
    assertEquals(IMAGE_FILTER, node.get(FILTER_NAME).asText());
    assertEquals(JPEG, node.get(FILTER_MIME_TYPES).get(0).asText());
    assertEquals(PNG, node.get(FILTER_MIME_TYPES).get(1).asText());

    // Create without filter name
    jsonBody.remove(FILTER_NAME);
    response = postEntity(jsonBody.toString(), uri, getToken(), false);
    node = mapper.readTree(response.getEntity().getContent());
    assertEquals(400, response.getStatusLine().getStatusCode());
    assertEquals("Filter name cannot be empty.", node.get("errors").get(0).get("message").asText());

    // Create without MIMEType
    jsonBody.put(FILTER_NAME, IMAGE_FILTER);
    jsonBody.remove(FILTER_MIME_TYPES);
    response = postEntity(jsonBody.toString(), uri, getToken(), false);
    node = mapper.readTree(response.getEntity().getContent());
    assertEquals(400, response.getStatusLine().getStatusCode());
    assertEquals("Need at least one MIME type.", node.get("errors").get(0).get("message").asText());

    // Create with bad MIMETypes
    ArrayNode badMimeTypes = jsonBody.putArray(FILTER_MIME_TYPES);
    badMimeTypes.add(BAD_MIME_TYPE);
    response = postEntity(jsonBody.toString(), uri, getToken(), false);
    node = mapper.readTree(response.getEntity().getContent());
    assertEquals(400, response.getStatusLine().getStatusCode());
    assertEquals(
        "Invalid MIMETypes found : " + BAD_MIME_TYPE,
        node.get("errors").get(0).get("message").asText());

    // Create without token
    response = postEntity(jsonBody.toString(), uri, null, false);
    assertEquals(403, response.getStatusLine().getStatusCode());
  }

  @Test(dependsOnMethods = "testCreateSearchFilter")
  public void testRetrieveSearchFilter() throws Exception {
    final String uri = searchFilterUri("");

    // Retrieve all search filters
    // getEntity already includes the check of when user is not authenticated
    final JsonNode initialFilters = getEntity(uri, getToken());
    assertEquals(1, initialFilters.size());

    // Retrieve a specific search filter
    final JsonNode filter = getEntity(searchFilterUri(newFilterId), getToken());
    assertEquals(newFilterId, filter.get(FILTER_ID).asText());
    assertEquals(IMAGE_FILTER, filter.get(FILTER_NAME).asText());
    assertEquals(JPEG, filter.get(FILTER_MIME_TYPES).get(0).asText());
    assertEquals(PNG, filter.get(FILTER_MIME_TYPES).get(1).asText());
  }

  @Test(dependsOnMethods = "testRetrieveSearchFilter")
  public void testUpdateSearchFilter() throws Exception {
    final String uri = searchFilterUri(newFilterId);

    // Update filter
    ObjectNode jsonBody = mapper.createObjectNode();
    jsonBody.put(FILTER_NAME, PDF_FILTER);
    ArrayNode mimeTypes = jsonBody.putArray(FILTER_MIME_TYPES);
    mimeTypes.add(PDF);
    HttpResponse response = putEntity(jsonBody.toString(), uri, getToken(), false);
    JsonNode node = mapper.readTree(response.getEntity().getContent());
    assertEquals(200, response.getStatusLine().getStatusCode());
    assertEquals(PDF_FILTER, node.get(FILTER_NAME).asText());
    assertEquals(PDF, node.get(FILTER_MIME_TYPES).get(0).asText());

    // Update with a bad filter ID
    response = putEntity(jsonBody.toString(), searchFilterUri(BAD_FILTER_ID), getToken(), true);
    assertEquals(404, response.getStatusLine().getStatusCode());

    // Update without filter name
    jsonBody.remove(FILTER_NAME);
    response = putEntity(jsonBody.toString(), uri, getToken(), true);
    assertEquals(400, response.getStatusLine().getStatusCode());

    // Update without MIMETypes
    jsonBody.put(FILTER_NAME, IMAGE_FILTER);
    jsonBody.remove(FILTER_MIME_TYPES);
    response = putEntity(jsonBody.toString(), uri, getToken(), true);
    assertEquals(400, response.getStatusLine().getStatusCode());

    // Update with bad MIMETypes
    ArrayNode badMimeTypes = jsonBody.putArray(FILTER_MIME_TYPES);
    badMimeTypes.add(BAD_MIME_TYPE);
    response = putEntity(jsonBody.toString(), uri, getToken(), true);
    assertEquals(400, response.getStatusLine().getStatusCode());

    // Update without token
    response = putEntity(jsonBody.toString(), uri, null, true);
    assertEquals(403, response.getStatusLine().getStatusCode());
  }

  @Test(dependsOnMethods = "testUpdateSearchFilter")
  public void testDeleteSearchFilter() throws Exception {
    final String uri = searchFilterUri(newFilterId);

    // Delete filter
    HttpResponse response = deleteResource(uri, getToken());
    assertEquals(response.getStatusLine().getStatusCode(), 200);

    // Delete with a bad filter ID
    response = deleteResource(searchFilterUri(BAD_FILTER_ID), getToken());
    assertEquals(404, response.getStatusLine().getStatusCode());

    // Delete without token
    response = deleteResource(searchFilterUri(BAD_FILTER_ID), null);
    assertEquals(403, response.getStatusLine().getStatusCode());
  }

  @Test(dependsOnMethods = "testDeleteSearchFilter")
  public void testBatchCreateSearchFilter() throws Exception {
    final String uri = searchFilterUri("");

    // Create two filters
    ArrayNode jsonBody = mapper.createArrayNode();
    ObjectNode imageFilter = buildTestingFilter(IMAGE_FILTER, PNG);
    ObjectNode pdfFilter = buildTestingFilter(PDF_FILTER, PDF);
    jsonBody.add(imageFilter);
    jsonBody.add(pdfFilter);

    HttpResponse response = putEntity(jsonBody.toString(), uri, getToken(), false);
    JsonNode result = mapper.readTree(response.getEntity().getContent());

    assertEquals(200, result.get(0).get(RESPONSE_STATUS).asInt());
    assertEquals(200, result.get(1).get(RESPONSE_STATUS).asInt());

    newFilterId = result.get(0).get(FILTER_ID).asText();
    secondeNewFilterId = result.get(1).get(FILTER_ID).asText();
  }

  @Test(dependsOnMethods = "testBatchCreateSearchFilter")
  public void testBatchUpdateSearchFilter() throws Exception {
    final String uri = searchFilterUri("");

    ArrayNode jsonBody = mapper.createArrayNode();
    ObjectNode pdfFilter = buildTestingFilter(PDF_FILTER, PDF);
    // The filter with newFilterId was an image filter. Now update to a PDF filter
    pdfFilter.put(FILTER_ID, newFilterId);

    ObjectNode imageFilter = buildTestingFilter(IMAGE_FILTER, PNG);
    imageFilter.put(FILTER_ID, secondeNewFilterId);
    jsonBody.add(pdfFilter);
    jsonBody.add(imageFilter);

    HttpResponse response = putEntity(jsonBody.toString(), uri, getToken(), false);
    JsonNode result = mapper.readTree(response.getEntity().getContent());
    assertEquals(200, result.get(0).get(RESPONSE_STATUS).asInt());
    assertEquals(200, result.get(1).get(RESPONSE_STATUS).asInt());

    // Retrieve and validate filters
    final JsonNode filters = getEntity(uri, getToken());
    assertEquals(newFilterId, filters.get(0).get(FILTER_ID).asText());
    assertEquals(PDF_FILTER, filters.get(0).get(FILTER_NAME).asText());
    assertEquals(PDF, filters.get(0).get(FILTER_MIME_TYPES).get(0).asText());

    // Update without filter name must fail
    pdfFilter.remove(FILTER_NAME);
    response = putEntity(jsonBody.toString(), uri, getToken(), false);
    result = mapper.readTree(response.getEntity().getContent());
    assertEquals(400, result.get(0).get(RESPONSE_STATUS).asInt());
  }

  @Test(dependsOnMethods = "testBatchUpdateSearchFilter")
  public void testBatchDeleteSearchFilter() throws Exception {
    final String uri = searchFilterUri("");

    // Delete the two filters created
    HttpResponse response =
        deleteResource(uri, getToken(), "ids", newFilterId, "ids", secondeNewFilterId);
    JsonNode result = mapper.readTree(response.getEntity().getContent());

    assertEquals(200, result.get(0).get(RESPONSE_STATUS).asInt());

    // Delete again with the deleted IDs
    response = deleteResource(uri, getToken(), "ids", newFilterId);
    result = mapper.readTree(response.getEntity().getContent());
    assertEquals(404, result.get(0).get(RESPONSE_STATUS).asInt());
  }

  private String searchFilterUri(String filterId) {
    return PathUtils.urlPath(context.getBaseUrl(), API_SEARCH_FILTER_PATH, filterId);
  }

  private ObjectNode buildTestingFilter(String name, String mimeType) {
    ObjectNode filter = mapper.createObjectNode();
    filter.put(FILTER_NAME, name);
    ArrayNode filterOneMimeTypes = filter.putArray(FILTER_MIME_TYPES);
    filterOneMimeTypes.add(mimeType);
    return filter;
  }
}
