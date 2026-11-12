/*
 * Copyright 2026 UBIK inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ubik;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ubik.cdr.CdrDetail;
import io.ubik.cdr.CdrItem;
import io.ubik.cdr.Direction;
import io.ubik.cdr.ListCdrsRequest;
import io.ubik.cdr.Operator;
import io.ubik.cdr.ResourceType;
import io.ubik.cdr.TimelineEntry;
import io.ubik.cdr.TimelineResource;
import io.ubik.internal.Json;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * client.cdrs() against the public CDR OpenAPI file: each request follows
 * it, each response example reads back without losing a value, and each
 * refusal becomes its exception.
 */
class CdrsTest {

  static final String TOKEN_PATH = "/api/v1/auth/oauth/token";
  private static final String LIST_PATH = "/api/v1/cdr/list";
  private static final String GET_TEMPLATE = "/api/v1/cdr/{id}";

  private StubServer server;
  private UBIK client;
  private String getExample;
  private String cdrId;

  @BeforeEach
  void setUp() throws Exception {
    server = new StubServer();
    server.route(
        TOKEN_PATH,
        200,
        "text/plain",
        PublicOpenAPI.AUTH.postResponseExample(TOKEN_PATH, "200", null));
    client = newClient(server);
    getExample = PublicOpenAPI.CDR.getResponseExample(GET_TEMPLATE, "200", "application/json", null);
    cdrId = JsonParser.parseString(getExample).getAsJsonObject().get("id").getAsString();
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  static UBIK newClient(StubServer server) {
    return UBIKOptions.newBuilder()
        .setOrgId(TestKeys.ORG_ID)
        .setCredentials(TestKeys.credentials())
        .setHost(server.host())
        .build()
        .getService();
  }

  @Test
  void getFollowsTheContractAndKeepsEveryValue() {
    server.route("/api/v1/cdr/" + cdrId, 200, "application/json", getExample);

    CdrDetail cdr = client.cdrs().get(cdrId);

    StubServer.Request request = server.requests("/api/v1/cdr/" + cdrId).get(0);
    PublicOpenAPI.CDR.assertValidRequest("GET", request.path, request.query, null, null);
    assertEquals("org_id=" + TestKeys.ORG_ID, request.query);
    String exampleToken =
        JsonParser.parseString(PublicOpenAPI.AUTH.postResponseExample(TOKEN_PATH, "200", null))
            .getAsJsonObject().get("access_token").getAsString();
    assertEquals("Bearer " + exampleToken, request.authorization);
    assertEquals(
        "login:" + TestKeys.ORG_ID + ":rw cdr:" + TestKeys.ORG_ID + ":r",
        form(server.requests(TOKEN_PATH).get(0).body, "scope"));

    // Every value of the example reads back from the Java object.
    assertContains(JsonParser.parseString(getExample), Json.GSON.toJsonTree(cdr), "$");
    assertEquals(cdrId, cdr.getId());
    assertNotNull(cdr.getDisposition());
  }

  @Test
  void getGivesEachSnapshotByItsType() {
    server.route("/api/v1/cdr/" + cdrId, 200, "application/json", getExample);
    CdrDetail cdr = client.cdrs().get(cdrId);

    int snapshots = 0;
    for (TimelineEntry entry : cdr.getTimeline()) {
      TimelineResource resource = entry.getResource();
      if (resource == null) {
        continue;
      }
      Object[] all = {
        resource.getPhoneSnapshot(),
        resource.getPhoneNumberSnapshot(),
        resource.getExtensionSnapshot(),
        resource.getIvrSnapshot()
      };
      int set = 0;
      for (Object snapshot : all) {
        set += snapshot == null ? 0 : 1;
      }
      boolean known =
          resource.getType().equals("PHONE")
              || resource.getType().equals("PHONE_NUMBER")
              || resource.getType().equals("EXTENSION")
              || resource.getType().equals("IVR");
      JsonObject raw = Json.GSON.toJsonTree(resource).getAsJsonObject();
      assertEquals(known && raw.has("snapshot") ? 1 : 0, set, resource.getType());
      snapshots += set;
    }
    assertTrue(snapshots > 0, "the example holds at least one snapshot");
  }

  @Test
  void listIteratesEveryPage() {
    JsonObject item =
        JsonParser.parseString(PublicOpenAPI.CDR.postResponseExample(LIST_PATH, "200", null))
            .getAsJsonObject().getAsJsonArray("data").get(0).getAsJsonObject();
    for (int page = 1; page <= 3; page++) {
      JsonArray data = new JsonArray();
      for (int i = 0; i < (page < 3 ? 2 : 1); i++) {
        JsonObject copy = item.deepCopy();
        copy.addProperty("id", "cdr-" + page + "-" + i);
        data.add(copy);
      }
      JsonObject body = new JsonObject();
      body.add("data", data);
      body.addProperty("total", 5);
      server.route(LIST_PATH, 200, "application/json", body.toString());
    }

    Page<CdrItem> first =
        client.cdrs().list(ListCdrsRequest.newBuilder().setNbPerPage(2).build());
    assertEquals(5, first.getTotal());
    List<String> ids = new ArrayList<>();
    for (CdrItem cdr : first.iterateAll()) {
      ids.add(cdr.getId());
    }

    assertEquals(List.of("cdr-1-0", "cdr-1-1", "cdr-2-0", "cdr-2-1", "cdr-3-0"), ids);
    List<StubServer.Request> requests = server.requests(LIST_PATH);
    assertEquals(3, requests.size());
    for (int i = 0; i < 3; i++) {
      StubServer.Request request = requests.get(i);
      PublicOpenAPI.CDR.assertValidRequest("POST", request.path, null, request.contentType, request.body);
      JsonObject limit =
          JsonParser.parseString(request.body).getAsJsonObject()
              .getAsJsonObject("list_query").getAsJsonObject("limit");
      assertEquals(i + 1, limit.get("page").getAsInt());
      assertEquals(2, limit.get("nb_per_page").getAsInt());
    }
  }

  @Test
  void listReadsTheExampleWithoutLosingAValue() {
    String example = PublicOpenAPI.CDR.postResponseExample(LIST_PATH, "200", null);
    server.route(LIST_PATH, 200, "application/json", example);
    Page<CdrItem> page = client.cdrs().list(ListCdrsRequest.newBuilder().build());
    JsonArray data = JsonParser.parseString(example).getAsJsonObject().getAsJsonArray("data");
    assertEquals(data.size(), page.getData().size());
    for (int i = 0; i < data.size(); i++) {
      assertContains(data.get(i), Json.GSON.toJsonTree(page.getData().get(i)), "$.data[" + i + "]");
    }
  }

  @Test
  void anEmptyPageEndsTheLoopEvenWhenTotalSaysMore() {
    server.route(LIST_PATH, 200, "application/json", "{\"data\":[],\"total\":10}");
    Page<CdrItem> page = client.cdrs().list(ListCdrsRequest.newBuilder().setNbPerPage(2).build());
    assertFalse(page.hasNextPage());
    assertNull(page.getNextPage());
    assertFalse(page.iterateAll().iterator().hasNext());
    assertEquals(1, server.requests(LIST_PATH).size());
  }

  @Test
  void listRequestCarriesEveryFilter() {
    server.route(LIST_PATH, 200, "application/json", "{\"data\":[],\"total\":0}");
    client.cdrs().list(
        ListCdrsRequest.newBuilder()
            .setResourceType(ResourceType.EXTENSION)
            .setResourceId("ext-1")
            .setStartTimestampMilli(Operator.GREATER_OR_EQUAL, 1794268800000L)
            .setStartTimestampMilli(Operator.LESS, 1794355200000L)
            .addDirection(Direction.INBOUND_PSTN)
            .addDirection(Direction.OUTBOUND_PSTN)
            .setIsAscending(true)
            .setNbPerPage(50)
            .build());

    StubServer.Request request = server.requests(LIST_PATH).get(0);
    PublicOpenAPI.CDR.assertValidRequest("POST", request.path, null, request.contentType, request.body);
    assertEquals(
        JsonParser.parseString(
            "{\"org_id\":\"" + TestKeys.ORG_ID + "\",\"resource_type\":\"EXTENSION\","
                + "\"list_query\":{\"filter\":{\"operation_list\":["
                + "{\"field_name\":\"resource_id\",\"operator\":\"=\",\"values\":[\"ext-1\"]},"
                + "{\"field_name\":\"start_timestamp_milli\",\"operator\":\">=\",\"values\":[\"1794268800000\"]},"
                + "{\"field_name\":\"start_timestamp_milli\",\"operator\":\"<\",\"values\":[\"1794355200000\"]},"
                + "{\"field_name\":\"direction\",\"operator\":\"=\",\"values\":[\"INBOUND_PSTN\",\"OUTBOUND_PSTN\"]}]},"
                + "\"sort\":{\"field_name\":\"start_timestamp_milli\",\"is_ascending\":true},"
                + "\"limit\":{\"nb_per_page\":50,\"page\":1}}}"),
        JsonParser.parseString(request.body));
  }

  @Test
  void aRefusedTokenIsRenewedOnce() {
    server.route("/api/v1/cdr/" + cdrId, 401, "text/plain", "token expired");
    server.route("/api/v1/cdr/" + cdrId, 200, "application/json", getExample);
    assertEquals(cdrId, client.cdrs().get(cdrId).getId());
    assertEquals(2, server.requests(TOKEN_PATH).size());
    assertEquals(2, server.requests("/api/v1/cdr/" + cdrId).size());
  }

  @Test
  void aTokenRefusedTwiceBecomesAUBIKException() {
    server.route("/api/v1/cdr/" + cdrId, 401, "text/plain", "token expired");
    UBIKException e = assertThrows(UBIKException.class, () -> client.cdrs().get(cdrId));
    assertEquals(401, e.getStatusCode());
    assertEquals(2, server.requests("/api/v1/cdr/" + cdrId).size());
  }

  @Test
  void eachRefusalBecomesItsException() {
    String notFound = PublicOpenAPI.CDR.getResponseExample(GET_TEMPLATE, "404", "text/plain", null);
    server.route("/api/v1/cdr/missing", 404, "text/plain", notFound);
    NotFoundException e404 =
        assertThrows(NotFoundException.class, () -> client.cdrs().get("missing"));
    assertEquals("HTTP 404: " + notFound, e404.getMessage());

    server.route("/api/v1/cdr/forbidden", 403, "text/plain", "missing scope cdr:r");
    PermissionDeniedException e403 =
        assertThrows(PermissionDeniedException.class, () -> client.cdrs().get("forbidden"));
    assertEquals(403, e403.getStatusCode());

    server.route("/api/v1/cdr/bad", 400, "text/plain", "org_id cannot be empty");
    UBIKException e400 = assertThrows(UBIKException.class, () -> client.cdrs().get("bad"));
    assertEquals(400, e400.getStatusCode());
    assertEquals("HTTP 400: org_id cannot be empty", e400.getMessage());

    server.route("/api/v1/cdr/error", 500, "text/plain", "");
    UBIKException e500 = assertThrows(UBIKException.class, () -> client.cdrs().get("error"));
    assertEquals("HTTP 500", e500.getMessage());
  }

  @Test
  void anIdIsEncodedInThePath() {
    server.route("/api/v1/cdr/a b", 200, "application/json", getExample);
    client.cdrs().get("a b");
    assertEquals(1, server.requests("/api/v1/cdr/a b").size());
  }

  /** Fails unless every value of expected is in actual, at the same place. */
  static void assertContains(JsonElement expected, JsonElement actual, String path) {
    if (expected.isJsonNull()) {
      return;
    }
    assertNotNull(actual, path + " is missing");
    if (expected.isJsonObject()) {
      assertTrue(actual.isJsonObject(), path + " is not an object");
      for (Map.Entry<String, JsonElement> entry : expected.getAsJsonObject().entrySet()) {
        assertContains(
            entry.getValue(), actual.getAsJsonObject().get(entry.getKey()), path + "." + entry.getKey());
      }
    } else if (expected.isJsonArray()) {
      assertTrue(actual.isJsonArray(), path + " is not an array");
      JsonArray e = expected.getAsJsonArray();
      JsonArray a = actual.getAsJsonArray();
      assertEquals(e.size(), a.size(), path + " size");
      for (int i = 0; i < e.size(); i++) {
        assertContains(e.get(i), a.get(i), path + "[" + i + "]");
      }
    } else {
      assertEquals(expected.getAsJsonPrimitive(), actual.getAsJsonPrimitive(), path);
    }
  }

  static String form(String body, String name) {
    for (String pair : body.split("&")) {
      if (pair.startsWith(name + "=")) {
        return URLDecoder.decode(pair.substring(name.length() + 1), StandardCharsets.UTF_8);
      }
    }
    return null;
  }
}
