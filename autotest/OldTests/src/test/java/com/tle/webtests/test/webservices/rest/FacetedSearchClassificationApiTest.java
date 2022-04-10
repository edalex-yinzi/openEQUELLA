package com.tle.webtests.test.webservices.rest;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import java.util.List;
import org.apache.http.HttpResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FacetedSearchClassificationApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "FacetedSearchApiTestClient";
  private static final String API_PATH = "api/settings/facetedsearch/classification/";
  private long classificationId;
  private ObjectNode validClassification;
  private ObjectNode invalidClassification;

  private static final String NAME = "good name";
  private static final String NEW_NAME = "better name";
  private static final long invalidId = 763311234511L;

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @BeforeClass
  @Override
  public void registerClients() throws Exception {
    super.registerClients();
    JsonNode node =
        mapper.readTree(
            getClass().getClassLoader().getResource("facetedsearchclassification.json"));

    validClassification = (ObjectNode) node.get("valid");
    invalidClassification = (ObjectNode) node.get("invalid");
  }

  @Test
  public void testCreateClassification() throws Exception {
    String uri = context.getBaseUrl() + API_PATH;
    HttpResponse response = postEntity(validClassification.toString(), uri, getToken(), false);
    assertEquals(201, response.getStatusLine().getStatusCode());
    JsonNode result = mapper.readTree(response.getEntity().getContent());
    classificationId = result.get("id").asLong();

    response = postEntity(invalidClassification.toString(), uri, getToken(), false);
    assertEquals(400, response.getStatusLine().getStatusCode());
  }

  @Test(dependsOnMethods = "testCreateClassification")
  public void testRetrieveClassification() throws Exception {
    final JsonNode classification = getEntity(validPath(), getToken());
    assertEquals(NAME, classification.get("name").asText());
  }

  @Test(dependsOnMethods = "testRetrieveClassification")
  public void testUpdateClassification() throws Exception {
    validClassification.put("name", NEW_NAME);
    HttpResponse response =
        putEntity(validClassification.toString(), validPath(), getToken(), false);
    assertEquals(200, response.getStatusLine().getStatusCode());
    JsonNode result = mapper.readTree(response.getEntity().getContent());
    assertEquals(NEW_NAME, result.get("name").asText());

    response = putEntity(invalidClassification.toString(), validPath(), getToken(), false);
    assertEquals(400, response.getStatusLine().getStatusCode());

    response = putEntity(validClassification.toString(), invalidPath(), getToken(), false);
    assertEquals(404, response.getStatusLine().getStatusCode());
  }

  @Test(dependsOnMethods = "testDeleteClassification")
  public void testBatchUpdateClassification() throws Exception {
    String uri = context.getBaseUrl() + API_PATH;
    ArrayNode jsonBody = mapper.createArrayNode();
    jsonBody.add(validClassification);
    jsonBody.add(invalidClassification);
    HttpResponse response = putEntity(jsonBody.toString(), uri, getToken(), false);
    assertEquals(207, response.getStatusLine().getStatusCode());

    JsonNode result = mapper.readTree(response.getEntity().getContent());
    assertEquals(200, result.get(0).get("status").asInt());
    assertEquals(400, result.get(1).get("status").asInt());
  }

  @Test(dependsOnMethods = "testUpdateClassification")
  public void testDeleteClassification() throws Exception {
    HttpResponse response = deleteResource(validPath(), getToken());
    assertEquals(200, response.getStatusLine().getStatusCode());

    response = deleteResource(invalidPath(), getToken());
    assertEquals(404, response.getStatusLine().getStatusCode());
  }

  @Test(dependsOnMethods = "testBatchUpdateClassification")
  public void testBatchDeleteClassification() throws Exception {
    String uri = context.getBaseUrl() + API_PATH;
    final JsonNode classification = getEntity(uri, getToken());
    classificationId = classification.get(0).get("id").asLong();

    HttpResponse response =
        deleteResource(uri, getToken(), "ids", classificationId, "ids", invalidId);
    assertEquals(207, response.getStatusLine().getStatusCode());

    JsonNode result = mapper.readTree(response.getEntity().getContent());
    assertEquals(200, result.get(0).get("status").asInt());
    assertEquals(404, result.get(1).get("status").asInt());
  }

  private String validPath() {
    return context.getBaseUrl() + API_PATH + classificationId;
  }

  private String invalidPath() {
    return context.getBaseUrl() + API_PATH + invalidId;
  }
}
