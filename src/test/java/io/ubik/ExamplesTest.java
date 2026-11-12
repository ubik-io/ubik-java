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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ubik.cdr.CdrDetail;
import io.ubik.cdr.CdrItem;
import io.ubik.cdr.RecordingChannel;
import io.ubik.cdr.RecordingItem;
import io.ubik.examples.ClientSetup;
import io.ubik.examples.DownloadRecordings;
import io.ubik.examples.FirstCall;
import io.ubik.examples.GetCdr;
import io.ubik.examples.HandleErrors;
import io.ubik.examples.ListCdrs;
import io.ubik.examples.ReceiveWebhooks;
import io.ubik.internal.Json;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Every snippet of the developer docs (src/examples) runs here against the
 * test server, which answers with the published OpenAPI examples. A snippet
 * that no longer compiles fails the build; one that no longer works fails
 * here. The region ids are the ones the docs cite.
 */
class ExamplesTest {

  /** The snippet ids the developer docs cite. */
  private static final List<String> DOCS_IDS =
      List.of(
          "client-setup", "first-call", "cdr-get", "cdr-list-window", "cdr-list-recorded",
          "channel-download", "mixdown-download", "channel-link", "mixdown-link",
          "webhook-verify", "webhook-test-event", "errors");

  private static final String LIST_PATH = "/api/v1/cdr/list";
  private static final String STORAGE = "/example-bucket/file";

  @TempDir Path dir;

  private StubServer server;
  private UBIK client;
  private String getExample;
  private CdrDetail exampleCdr;

  @BeforeEach
  void setUp() throws Exception {
    server = new StubServer();
    server.route(
        CdrsTest.TOKEN_PATH, 200, "text/plain",
        PublicOpenAPI.AUTH.postResponseExample(CdrsTest.TOKEN_PATH, "200", null));
    client = CdrsTest.newClient(server);
    getExample = PublicOpenAPI.CDR.getResponseExample("/api/v1/cdr/{id}", "200", "application/json", null);
    exampleCdr = Json.GSON.fromJson(getExample, CdrDetail.class);
    server.route("/api/v1/cdr/" + exampleCdr.getId(), 200, "application/json", getExample);
    server.route(LIST_PATH, 200, "application/json", PublicOpenAPI.CDR.postResponseExample(LIST_PATH, "200", null));
    server.route(STORAGE, 200, "application/octet-stream", "invented audio");
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  @Test
  void eachDocsIdIsOneRegionOfOneExample() throws IOException {
    Pattern marker = Pattern.compile("// \\[(START|END) ([a-z-]+)\\]");
    List<String> starts = new ArrayList<>();
    try (Stream<Path> files = Files.walk(Path.of("src/examples/java"))) {
      for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
        String open = null;
        Matcher m = marker.matcher(Files.readString(file));
        while (m.find()) {
          if (m.group(1).equals("START")) {
            assertNull(open, file + ": " + m.group(2) + " starts inside " + open);
            open = m.group(2);
            starts.add(open);
          } else {
            assertEquals(open, m.group(2), file + ": END " + m.group(2) + " without its START");
            open = null;
          }
        }
        assertNull(open, file + ": " + open + " has no END");
      }
    }
    assertEquals(new TreeSet<>(DOCS_IDS), new TreeSet<>(starts));
    assertEquals(DOCS_IDS.size(), starts.size(), "each id once");
  }

  @Test
  void clientSetup() throws IOException {
    Path keyFile = dir.resolve("ubik-api-key.json");
    Files.writeString(keyFile, TestKeys.keyFileJson());
    UBIK created = ClientSetup.create(keyFile);
    assertEquals("7d1f3a5c-2b4e-4f6a-8c9d-0e1f2a3b4c5d", created.getOptions().getOrgId());
    assertEquals(TestKeys.KEY_FILE_ID, created.getOptions().getCredentials().getId());
  }

  @Test
  void firstCall() {
    String printed = captureOut(() -> FirstCall.run(client));
    JsonObject example = JsonParser.parseString(PublicOpenAPI.CDR.postResponseExample(LIST_PATH, "200", null)).getAsJsonObject();
    StringBuilder expected = new StringBuilder();
    example.getAsJsonArray("data").forEach(c -> expected.append(c.getAsJsonObject().get("id").getAsString()).append(System.lineSeparator()));
    assertEquals(expected.toString(), printed);
    JsonObject limit = JsonParser.parseString(server.requests(LIST_PATH).get(0).body).getAsJsonObject()
        .getAsJsonObject("list_query").getAsJsonObject("limit");
    assertEquals(10, limit.get("nb_per_page").getAsInt());
  }

  @Test
  void cdrGet() {
    assertEquals(exampleCdr.getId(), GetCdr.run(client, exampleCdr.getId()).getId());
  }

  @Test
  void cdrListWindow() {
    List<CdrItem> cdrs = ListCdrs.window(client, 1794268800000L, 1794355200000L);
    assertTrue(!cdrs.isEmpty());
    StubServer.Request request = server.requests(LIST_PATH).get(0);
    PublicOpenAPI.CDR.assertValidRequest("POST", request.path, null, request.contentType, request.body);
    JsonObject query = JsonParser.parseString(request.body).getAsJsonObject().getAsJsonObject("list_query");
    assertEquals(true, query.getAsJsonObject("sort").get("is_ascending").getAsBoolean());
    assertEquals(2, query.getAsJsonObject("filter").getAsJsonArray("operation_list").size());
  }

  @Test
  void cdrListRecorded() {
    for (CdrItem cdr : ListCdrs.recorded(client)) {
      assertTrue(cdr.getRecordingSessionCount() > 0);
    }
  }

  @Test
  void downloads() throws IOException {
    assertTrue(!exampleCdr.getRecordingSessionList().isEmpty(), "the example has recordings");
    int mixdowns = 0;
    int channels = 0;
    for (RecordingItem recording : exampleCdr.getRecordingSessionList()) {
      routeLink("mixdown", recording.getMixdownFileId());
      mixdowns++;
      for (RecordingChannel channel : recording.getChannelList()) {
        routeLink("channel", channel.getFileId());
        channels++;
      }
    }
    DownloadRecordings.mixdowns(client, exampleCdr, dir);
    DownloadRecordings.channels(client, exampleCdr, dir);
    assertEquals(mixdowns, countFiles(".mp3"));
    assertEquals(channels, countFiles(".ogg"));

    RecordingItem first = exampleCdr.getRecordingSessionList().get(0);
    assertEquals(server.host() + STORAGE, DownloadRecordings.mixdownLink(client, exampleCdr.getId(), first.getMixdownFileId()));
    assertEquals(server.host() + STORAGE, DownloadRecordings.channelLink(client, exampleCdr.getId(), first.getChannelList().get(0).getFileId()));
  }

  @Test
  void webhook() throws Exception {
    @SuppressWarnings("unchecked")
    Map<String, Object> vector = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>)
        ((Map<String, Object>) PublicOpenAPI.WEBHOOK.model().getExtensions().get("x-webhooks")).get("cdr-add"))
        .get("post")).get("x-signature-test-vector");
    String secret = (String) vector.get("secret");
    String now = Long.toString(System.currentTimeMillis() / 1000);

    String body = ((String) vector.get("body")).replace("8f1c2d3e-4a5b-4c6d-9e7f-0a1b2c3d4e5f", exampleCdr.getId());
    String signature = sign(secret, "evt-1", now, body);
    String printed = captureOut(() -> assertEquals(200, ReceiveWebhooks.handle(client, secret, "evt-1", now, signature, body)));
    assertEquals("Call completed: " + exampleCdr.getId() + System.lineSeparator(), printed);

    assertEquals(400, ReceiveWebhooks.handle(client, secret, "evt-1", now, "v1,AAAA", body));

    String testEvent = body.replace(exampleCdr.getId(), "00000000-0000-0000-0000-000000000000");
    int before = server.requests().size();
    assertEquals(200, ReceiveWebhooks.handle(client, secret, "evt-2", now, sign(secret, "evt-2", now, testEvent), testEvent));
    assertEquals(before, server.requests().size(), "a test event reads no CDR");
  }

  @Test
  void errors() {
    server.route("/api/v1/cdr/missing", 404, "text/plain", "cdr not found");
    assertNull(HandleErrors.getOrNull(client, "missing"));
    server.route("/api/v1/cdr/forbidden", 403, "text/plain", "missing scope");
    assertThrows(PermissionDeniedException.class, () -> HandleErrors.getOrNull(client, "forbidden"));
    server.route("/api/v1/cdr/broken", 500, "text/plain", "");
    assertNull(HandleErrors.getOrNull(client, "broken"));
    assertEquals(exampleCdr.getId(), HandleErrors.getOrNull(client, exampleCdr.getId()).getId());
  }

  private void routeLink(String kind, String fileId) {
    server.route(
        "/api/v1/cdr/" + exampleCdr.getId() + "/recording/dl/" + kind + "/" + fileId,
        200, "application/json", "{\"download_url\":\"" + server.host() + STORAGE + "\"}");
  }

  private long countFiles(String suffix) throws IOException {
    try (Stream<Path> files = Files.list(dir)) {
      return files.filter(p -> p.toString().endsWith(suffix)).count();
    }
  }

  private static String captureOut(Runnable runnable) {
    PrintStream original = System.out;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    try {
      runnable.run();
    } finally {
      System.setOut(original);
    }
    return out.toString(StandardCharsets.UTF_8);
  }

  private static String sign(String secret, String id, String timestamp, String body) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(Base64.getDecoder().decode(secret.substring("whsec_".length())), "HmacSHA256"));
    return "v1," + Base64.getEncoder().encodeToString(mac.doFinal((id + "." + timestamp + "." + body).getBytes(StandardCharsets.UTF_8)));
  }
}
