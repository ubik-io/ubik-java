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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Token requests follow the public authentication OpenAPI file, and tokens are
 * reused until shortly before they expire. Without the renewal, a long-lived
 * client would send expired tokens and get 401 on every call; without the
 * cache, it would ask for a token before each call.
 */
class AccessTokenRefresherTest {

  private static final String CDR_SCOPE = "cdr:" + TestKeys.ORG_ID + ":r";
  private static final String TOKEN_PATH = "/api/v1/auth/oauth/token";
  private static final Instant START = Instant.ofEpochSecond(1794268800);

  private StubServer server;
  private MutableClock clock;
  private AccessTokenRefresher refresher;

  @BeforeEach
  void setUp() throws Exception {
    server = new StubServer();
    respondWithToken(PublicOpenAPI.AUTH.postResponseExample(TOKEN_PATH, "200", null));
    clock = new MutableClock(START);
    refresher =
        new AccessTokenRefresher(
            TestKeys.credentials(), server.host(), HttpClient.newHttpClient(), clock);
  }

  @AfterEach
  void tearDown() {
    server.close();
  }

  @Test
  void requestFollowsThePublicOpenAPI() {
    String token = refresher.getAccessToken(CDR_SCOPE);

    String example = PublicOpenAPI.AUTH.postResponseExample(TOKEN_PATH, "200", null);
    assertEquals(
        JsonParser.parseString(example).getAsJsonObject().get("access_token").getAsString(),
        token);

    assertEquals(1, server.requests().size());
    StubServer.Request request = server.requests().get(0);
    PublicOpenAPI.AUTH.assertValidRequest(
        request.method, request.path, request.contentType, request.body);

    Map<String, String> form = parseForm(request.body);
    assertEquals("urn:ietf:params:oauth:grant-type:jwt-bearer", form.get("grant_type"));
    assertEquals(CDR_SCOPE, form.get("scope"));
    JsonObject claims = claims(form.get("assertion"));
    assertEquals(server.host() + TOKEN_PATH, claims.get("aud").getAsString());
    assertEquals(START.getEpochSecond(), claims.get("iat").getAsLong());
    assertTrue(request.userAgent.startsWith("ubik-java/"), request.userAgent);
  }

  @Test
  void reusesTheTokenUntilAMinuteBeforeItExpires() {
    // The example token lasts 900 s: renewed from 840 s on.
    refresher.getAccessToken(CDR_SCOPE);
    clock.set(START.plusSeconds(839));
    refresher.getAccessToken(CDR_SCOPE);
    assertEquals(1, server.requests().size());

    clock.set(START.plusSeconds(840));
    refresher.getAccessToken(CDR_SCOPE);
    assertEquals(2, server.requests().size());

    String firstJti = claims(parseForm(server.requests().get(0).body).get("assertion")).get("jti").getAsString();
    String secondJti = claims(parseForm(server.requests().get(1).body).get("assertion")).get("jti").getAsString();
    assertNotEquals(firstJti, secondJti, "each assertion has a new jti");
    assertEquals(
        START.plusSeconds(840).getEpochSecond(),
        claims(parseForm(server.requests().get(1).body).get("assertion")).get("iat").getAsLong());
  }

  @Test
  void aShortTokenIsRenewedHalfwayThrough() {
    respondWithToken("{\"access_token\":\"short\",\"id_token\":\"\",\"token_type\":\"Bearer\",\"expires_in\":60}");
    refresher.getAccessToken(CDR_SCOPE);
    clock.set(START.plusSeconds(29));
    refresher.getAccessToken(CDR_SCOPE);
    assertEquals(1, server.requests().size());
    clock.set(START.plusSeconds(30));
    refresher.getAccessToken(CDR_SCOPE);
    assertEquals(2, server.requests().size());
  }

  @Test
  void keepsOneTokenPerScope() {
    String otherScope = "webhook:" + TestKeys.ORG_ID + ":r";
    refresher.getAccessToken(CDR_SCOPE);
    refresher.getAccessToken(otherScope);
    refresher.getAccessToken(CDR_SCOPE);
    refresher.getAccessToken(otherScope);
    assertEquals(2, server.requests().size());
    assertEquals(CDR_SCOPE, parseForm(server.requests().get(0).body).get("scope"));
    assertEquals(otherScope, parseForm(server.requests().get(1).body).get("scope"));
  }

  @Test
  void eachRefusalOfThePublicFileBecomesAUBIKException() {
    String[][] refusals = {
      {"400", "assertion_empty"},
      {"400", "assertion_not_a_jwt"},
      {"400", "lifetime_too_long"},
      {"400", "jti_reused"},
      {"400", "scope_not_held"},
      {"401", "assertion_refused"},
      {"401", "scope_unreadable"},
    };
    for (String[] refusal : refusals) {
      String body = PublicOpenAPI.AUTH.postResponseExample(TOKEN_PATH, refusal[0], refusal[1]);
      JsonObject payload = JsonParser.parseString(body).getAsJsonObject();
      server.respond(Integer.parseInt(refusal[0]), "application/json", body);

      UBIKException e =
          assertThrows(UBIKException.class, () -> refresher.getAccessToken(CDR_SCOPE), refusal[1]);
      assertEquals(Integer.parseInt(refusal[0]), e.getStatusCode(), refusal[1]);
      assertEquals(
          "Getting an access token failed: "
              + payload.get("error").getAsString()
              + ": "
              + payload.get("error_description").getAsString(),
          e.getMessage(),
          refusal[1]);
    }
  }

  @Test
  void aServerErrorBecomesAUBIKException() {
    server.respond(500, "application/json", PublicOpenAPI.AUTH.postResponseExample(TOKEN_PATH, "500", null));
    UBIKException e = assertThrows(UBIKException.class, () -> refresher.getAccessToken(CDR_SCOPE));
    assertEquals(500, e.getStatusCode());
    assertTrue(e.getMessage().startsWith("Getting an access token failed: server_error"), e.getMessage());
  }

  @Test
  void aResponseThatIsntJsonBecomesAUBIKException() {
    server.respond(502, "text/html", "<html>Bad Gateway</html>");
    UBIKException e = assertThrows(UBIKException.class, () -> refresher.getAccessToken(CDR_SCOPE));
    assertEquals(502, e.getStatusCode());
    assertEquals("Getting an access token failed: HTTP 502", e.getMessage());
  }

  @Test
  void aRefusalIsNotKept() {
    server.respond(401, "application/json", PublicOpenAPI.AUTH.postResponseExample(TOKEN_PATH, "401", "assertion_refused"));
    assertThrows(UBIKException.class, () -> refresher.getAccessToken(CDR_SCOPE));
    respondWithToken(PublicOpenAPI.AUTH.postResponseExample(TOKEN_PATH, "200", null));
    refresher.getAccessToken(CDR_SCOPE);
    assertEquals(2, server.requests().size());
  }

  @Test
  void anUnreachableHostBecomesAUBIKException() {
    server.close();
    UBIKException e = assertThrows(UBIKException.class, () -> refresher.getAccessToken(CDR_SCOPE));
    assertEquals(0, e.getStatusCode());
  }

  /** The token endpoint answers 200 with Content-Type: text/plain, as the public file says. */
  private void respondWithToken(String body) {
    server.respond(200, "text/plain; charset=utf-8", body);
  }

  private static Map<String, String> parseForm(String body) {
    Map<String, String> form = new HashMap<>();
    for (String pair : body.split("&")) {
      int equals = pair.indexOf('=');
      form.put(
          URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8),
          URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8));
    }
    return form;
  }

  private static JsonObject claims(String assertion) {
    String payload = assertion.split("\\.")[1];
    return JsonParser.parseString(
            new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private static final class MutableClock extends Clock {
    private volatile Instant now;

    MutableClock(Instant now) {
      this.now = now;
    }

    void set(Instant now) {
      this.now = now;
    }

    @Override
    public Instant instant() {
      return now;
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }
  }
}
