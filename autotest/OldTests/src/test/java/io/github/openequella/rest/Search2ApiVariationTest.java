package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.testng.annotations.Test;

/** This test class focuses on the variation of search2 ('POST search2') */
public class Search2ApiVariationTest extends AbstractRestApiTest {
  private final String SEARCH_API_ENDPOINT = getTestConfig().getInstitutionUrl() + "api/search2";
  private final ObjectMapper mapper = new ObjectMapper();

  // Unlike GET API, POST API returns 400 - "Missing required field status"
  @Test(description = "Search by an invalid item status")
  public void invalidItemStatusSearch() throws IOException {
    ArrayList<String> status = new ArrayList<>();
    status.add("ALIVE");
    doSearch(400, mapper.createObjectNode().set("status", mapper.valueToTree(status)));
  }

  @Test(description = "Search for a large number of MIME types")
  public void largeNumberOfParametersSearch() throws IOException {
    // Create a large number of mime-types which include an existing one,
    // to make sure the endpoint still returns the correct result.
    ArrayList<String> mimeTypes = new ArrayList<>();
    mimeTypes.add("text/plain");
    for (int i = 1; i <= 100; i++) {
      mimeTypes.add("text/plain" + Integer.toString(i));
    }

    ObjectNode payload = mapper.createObjectNode();
    payload.set("mimeTypes", mapper.valueToTree(mimeTypes));

    JsonNode result = doSearch(200, payload);
    assertEquals(result.get("available").asInt(), 6);
  }

  private JsonNode doSearch(int expectedCode, JsonNode payload) throws IOException {
    final PostMethod method = new PostMethod(SEARCH_API_ENDPOINT);
    method.setRequestEntity(
        new StringRequestEntity(payload.toString(), "application/json", "UTF-8"));

    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, expectedCode);

    return mapper.readTree(method.getResponseBody());
  }
}
