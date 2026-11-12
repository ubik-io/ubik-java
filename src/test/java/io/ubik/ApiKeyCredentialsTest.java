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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/**
 * The key file is read as UBIK's key creation writes it, and each assertion
 * follows the token endpoint's published rules (the authentication OpenAPI file,
 * GetTokenRequest.assertion): a broken rule means every token request is
 * refused with 400 or 401.
 */
class ApiKeyCredentialsTest {

  private static final String AUDIENCE = "https://ubik.io/api/v1/auth/oauth/token";

  @Test
  void readsTheKeyFile() {
    ApiKeyCredentials credentials = TestKeys.credentials();
    assertEquals(TestKeys.KEY_FILE_ID, credentials.getId());
    assertEquals(TestKeys.KEY_ID, credentials.getKeyId());
  }

  @Test
  void refusesAFileWithoutAField() {
    for (String field : new String[] {"id", "key_id", "private_key"}) {
      JsonObject file = JsonParser.parseString(TestKeys.keyFileJson()).getAsJsonObject();
      file.remove(field);
      IllegalArgumentException e =
          assertThrows(IllegalArgumentException.class, () -> read(file.toString()));
      assertEquals("The key file has no " + field, e.getMessage());
    }
  }

  @Test
  void refusesAnEmptyField() {
    JsonObject file = JsonParser.parseString(TestKeys.keyFileJson()).getAsJsonObject();
    file.addProperty("key_id", "");
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> read(file.toString()));
    assertEquals("The key file's key_id is empty", e.getMessage());
  }

  @Test
  void refusesAFileThatIsntJson() {
    assertThrows(IllegalArgumentException.class, () -> read("not json"));
    assertThrows(IllegalArgumentException.class, () -> read("[]"));
  }

  @Test
  void refusesAKeyThatIsntPkcs8Pem() {
    JsonObject file = JsonParser.parseString(TestKeys.keyFileJson()).getAsJsonObject();
    file.addProperty("private_key", "-----BEGIN RSA PRIVATE KEY-----\nAAAA\n-----END RSA PRIVATE KEY-----\n");
    assertThrows(IllegalArgumentException.class, () -> read(file.toString()));
  }

  @Test
  void refusesAKeyThatIsntRsa() throws Exception {
    KeyPairGenerator ec = KeyPairGenerator.getInstance("EC");
    ec.initialize(256);
    String pem =
        "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getEncoder().encodeToString(ec.generateKeyPair().getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----\n";
    JsonObject file = JsonParser.parseString(TestKeys.keyFileJson()).getAsJsonObject();
    file.addProperty("private_key", pem);
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> read(file.toString()));
    assertEquals("The key file's private_key isn't an RSA key", e.getMessage());
  }

  @Test
  void assertionFollowsTheTokenEndpointRules() throws Exception {
    Instant now = Instant.ofEpochSecond(1794268800);
    String assertion = TestKeys.credentials().signAssertion(AUDIENCE, now, "jti-1");
    String[] parts = assertion.split("\\.");
    assertEquals(3, parts.length);

    JsonObject header = decode(parts[0]);
    assertEquals("RS256", header.get("alg").getAsString(), "Header alg: RS256");
    assertEquals(TestKeys.KEY_ID, header.get("kid").getAsString(), "Header kid: the key_id");

    JsonObject claims = decode(parts[1]);
    assertEquals(TestKeys.KEY_FILE_ID, claims.get("iss").getAsString(), "iss: the key file's id");
    assertEquals(TestKeys.KEY_FILE_ID, claims.get("sub").getAsString(), "sub: the key file's id");
    assertEquals(AUDIENCE, claims.get("aud").getAsString(), "aud: the token endpoint");
    assertEquals(now.getEpochSecond(), claims.get("iat").getAsLong(), "iat: now");
    long lifetime = claims.get("exp").getAsLong() - claims.get("iat").getAsLong();
    assertTrue(
        lifetime > 0 && lifetime <= ApiKeyCredentials.MAX_ASSERTION_LIFETIME_SECONDS,
        "exp: at most 5 minutes after iat, was " + lifetime);
    assertEquals("jti-1", claims.get("jti").getAsString(), "jti: sent");

    Signature verifier = Signature.getInstance("SHA256withRSA");
    verifier.initVerify(TestKeys.KEY_PAIR.getPublic());
    verifier.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
    assertTrue(verifier.verify(Base64.getUrlDecoder().decode(parts[2])), "signed with the key");
  }

  private static ApiKeyCredentials read(String json) throws Exception {
    return ApiKeyCredentials.fromStream(
        new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
  }

  private static JsonObject decode(String part) {
    return JsonParser.parseString(
            new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }
}
