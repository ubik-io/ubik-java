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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ubik.cdr.RecordingDownload;
import io.ubik.cdr.RecordingNotReadyException;
import io.ubik.cdr.RecordingUnavailableException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * client.recordings() against the public CDR OpenAPI file: links, downloads,
 * and each published 404 and 409 example. A download never sends the UBIK
 * token to the storage link.
 */
class RecordingsTest {

  private static final String MIXDOWN_TEMPLATE = "/api/v1/cdr/{id}/recording/dl/mixdown/{file_id}";
  private static final String CHANNEL_TEMPLATE = "/api/v1/cdr/{id}/recording/dl/channel/{file_id}";
  private static final String MIXDOWN = "/api/v1/cdr/cdr-1/recording/dl/mixdown/file-1";
  private static final String CHANNEL = "/api/v1/cdr/cdr-1/recording/dl/channel/file-2";
  private static final String STORAGE = "/example-bucket/file.mp3";
  private static final byte[] MP3 = "ID3 invented audio".getBytes(StandardCharsets.US_ASCII);

  @TempDir Path dir;

  private StubServer server;
  private UBIK client;

  @BeforeEach
  void setUp() throws Exception {
    server = new StubServer();
    server.route(
        CdrsTest.TOKEN_PATH,
        200,
        "text/plain",
        PublicOpenAPI.AUTH.postResponseExample(CdrsTest.TOKEN_PATH, "200", null));
    client = CdrsTest.newClient(server);
    server.route(STORAGE, 200, "audio/mpeg", MP3);
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  /** The published link example, pointed at the test server's storage path. */
  private String link(String template) {
    JsonObject example =
        JsonParser.parseString(
                PublicOpenAPI.CDR.getResponseExample(template, "200", "application/json", null))
            .getAsJsonObject();
    String url = example.get("download_url").getAsString();
    example.addProperty(
        "download_url", server.host() + STORAGE + url.substring(url.indexOf('?')));
    return example.toString();
  }

  @Test
  void mixdownLinkFollowsTheContract() {
    String example = PublicOpenAPI.CDR.getResponseExample(MIXDOWN_TEMPLATE, "200", "application/json", null);
    server.route(MIXDOWN, 200, "application/json", example);
    RecordingDownload link = client.recordings().getMixdownFileLink("cdr-1", "file-1");
    assertEquals(
        JsonParser.parseString(example).getAsJsonObject().get("download_url").getAsString(),
        link.getDownloadUrl());
    StubServer.Request request = server.requests(MIXDOWN).get(0);
    PublicOpenAPI.CDR.assertValidRequest("GET", request.path, request.query, null, null);
  }

  @Test
  void channelLinkFollowsTheContract() {
    String example = PublicOpenAPI.CDR.getResponseExample(CHANNEL_TEMPLATE, "200", "application/json", null);
    server.route(CHANNEL, 200, "application/json", example);
    client.recordings().getChannelFileLink("cdr-1", "file-2");
    StubServer.Request request = server.requests(CHANNEL).get(0);
    PublicOpenAPI.CDR.assertValidRequest("GET", request.path, request.query, null, null);
  }

  @Test
  void downloadsTheMixdownToAFileWithoutTheToken() throws Exception {
    server.route(MIXDOWN, 200, "application/json", link(MIXDOWN_TEMPLATE));
    Path file = dir.resolve("call.mp3");
    client.recordings().downloadMixdownFile("cdr-1", "file-1", file);
    assertArrayEquals(MP3, Files.readAllBytes(file));
    assertNull(server.requests(STORAGE).get(0).authorization, "no UBIK token to storage");
  }

  @Test
  void downloadsAChannelToAStream() {
    server.route(CHANNEL, 200, "application/json", link(CHANNEL_TEMPLATE));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    client.recordings().downloadChannelFile("cdr-1", "file-2", out);
    assertArrayEquals(MP3, out.toByteArray());
    assertNull(server.requests(STORAGE).get(0).authorization, "no UBIK token to storage");
  }

  @Test
  void eachDownloadAsksForANewLink() {
    server.route(MIXDOWN, 200, "application/json", link(MIXDOWN_TEMPLATE));
    client.recordings().downloadMixdownFile("cdr-1", "file-1", new ByteArrayOutputStream());
    client.recordings().downloadMixdownFile("cdr-1", "file-1", new ByteArrayOutputStream());
    assertEquals(2, server.requests(MIXDOWN).size());
  }

  @Test
  void aStorageRefusalLeavesNoFile() throws Exception {
    StubServer storage = new StubServer();
    try {
      storage.route(STORAGE, 403, "application/xml", "<Error>SignatureDoesNotMatch</Error>");
      JsonObject body = new JsonObject();
      body.addProperty("download_url", storage.host() + STORAGE + "?X-Goog-Signature=x");
      server.route(MIXDOWN, 200, "application/json", body.toString());
      Path file = dir.resolve("call.mp3");
      UBIKException e =
          assertThrows(
              UBIKException.class,
              () -> client.recordings().downloadMixdownFile("cdr-1", "file-1", file));
      assertEquals(403, e.getStatusCode());
      assertFalse(Files.exists(file));
    } finally {
      storage.close();
    }
  }

  @Test
  void eachPublishedConflictBecomesItsException() {
    String[][] cases = {
      {"recording_complete", "RECORDING_COMPLETE", "ready"},
      {"in_progress", "IN_PROGRESS", "ready"},
      {"failed_too_many_retries", "FAILED_TOO_MANY_RETRIES", "unavailable"},
      {"expired", "EXPIRED", "unavailable"},
    };
    for (String template : new String[] {MIXDOWN_TEMPLATE, CHANNEL_TEMPLATE}) {
      for (String[] c : cases) {
        String body = PublicOpenAPI.CDR.getResponseExample(template, "409", "text/plain", c[0]);
        String path = "/api/v1/cdr/cdr-409-" + c[0] + "/recording/dl/" + (template.equals(MIXDOWN_TEMPLATE) ? "mixdown" : "channel") + "/f";
        server.route(path, 409, "text/plain", body);
        UBIKException e =
            assertThrows(
                UBIKException.class,
                () -> {
                  if (template.equals(MIXDOWN_TEMPLATE)) {
                    client.recordings().getMixdownFileLink("cdr-409-" + c[0], "f");
                  } else {
                    client.recordings().getChannelFileLink("cdr-409-" + c[0], "f");
                  }
                });
        assertEquals(409, e.getStatusCode(), c[0]);
        if (c[2].equals("ready")) {
          assertEquals(RecordingNotReadyException.class, e.getClass(), c[0]);
          assertEquals(c[1], ((RecordingNotReadyException) e).getState());
        } else {
          assertEquals(RecordingUnavailableException.class, e.getClass(), c[0]);
          assertEquals(c[1], ((RecordingUnavailableException) e).getState());
        }
      }
    }
  }

  @Test
  void anUnknownConflictStaysAUBIKException() {
    server.route(MIXDOWN, 409, "text/plain", "recording is SOMETHING_NEW");
    UBIKException e =
        assertThrows(UBIKException.class, () -> client.recordings().getMixdownFileLink("cdr-1", "file-1"));
    assertEquals(UBIKException.class, e.getClass());
    assertEquals("HTTP 409: recording is SOMETHING_NEW", e.getMessage());
  }

  @Test
  void eachPublishedNotFoundBecomesNotFoundException() {
    for (String name : new String[] {"cdr_not_found", "file_not_found"}) {
      String body = PublicOpenAPI.CDR.getResponseExample(MIXDOWN_TEMPLATE, "404", "text/plain", name);
      server.route("/api/v1/cdr/" + name + "/recording/dl/mixdown/f", 404, "text/plain", body);
      NotFoundException e =
          assertThrows(
              NotFoundException.class, () -> client.recordings().getMixdownFileLink(name, "f"));
      assertEquals("HTTP 404: " + body, e.getMessage());
    }
  }
}
