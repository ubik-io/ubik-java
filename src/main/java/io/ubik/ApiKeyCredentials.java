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

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * The key file of a UBIK API key, as downloaded when the key was created.
 *
 * <pre>{@code
 * ApiKeyCredentials credentials;
 * try (InputStream in = new FileInputStream("/path/to/key-file.json")) {
 *   credentials = ApiKeyCredentials.fromStream(in);
 * }
 * }</pre>
 *
 * <p>The private key never leaves this object: it only signs the assertions
 * exchanged for access tokens.
 */
public final class ApiKeyCredentials {

  /** Longest assertion lifetime the token endpoint accepts. */
  static final long MAX_ASSERTION_LIFETIME_SECONDS = 300;

  /** Lifetime of each assertion this library signs. */
  static final long ASSERTION_LIFETIME_SECONDS = 60;

  private final String id;
  private final String keyId;
  private final PrivateKey privateKey;

  private ApiKeyCredentials(String id, String keyId, PrivateKey privateKey) {
    this.id = id;
    this.keyId = keyId;
    this.privateKey = privateKey;
  }

  /**
   * Reads a key file.
   *
   * @param in the key file's JSON; the stream is read to its end, not closed
   * @return the credentials
   * @throws IOException when the stream can't be read
   * @throws IllegalArgumentException when the file isn't a UBIK API key file
   */
  public static ApiKeyCredentials fromStream(InputStream in) throws IOException {
    Objects.requireNonNull(in, "in");
    String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    JsonObject file;
    try {
      file = JsonParser.parseString(json).getAsJsonObject();
    } catch (JsonParseException | IllegalStateException e) {
      throw new IllegalArgumentException("The key file isn't a JSON object", e);
    }
    String id = requireString(file, "id");
    String keyId = requireString(file, "key_id");
    String privateKey = requireString(file, "private_key");
    return new ApiKeyCredentials(id, keyId, parsePrivateKey(privateKey));
  }

  /** The key file's id: the iss and sub of each assertion. */
  public String getId() {
    return id;
  }

  /** The key file's key_id: the kid header of each assertion. */
  public String getKeyId() {
    return keyId;
  }

  /**
   * Signs a JWT-bearer assertion for the token endpoint.
   *
   * @param audience the token endpoint's URL
   * @param now the assertion's iat
   * @param jti a new unique id
   */
  String signAssertion(String audience, Instant now, String jti) {
    JsonObject header = new JsonObject();
    header.addProperty("alg", "RS256");
    header.addProperty("kid", keyId);
    header.addProperty("typ", "JWT");

    long iat = now.getEpochSecond();
    JsonObject claims = new JsonObject();
    claims.addProperty("iss", id);
    claims.addProperty("sub", id);
    claims.addProperty("aud", audience);
    claims.addProperty("iat", iat);
    claims.addProperty("exp", iat + ASSERTION_LIFETIME_SECONDS);
    claims.addProperty("jti", jti);

    Base64.Encoder base64 = Base64.getUrlEncoder().withoutPadding();
    String signingInput =
        base64.encodeToString(header.toString().getBytes(StandardCharsets.UTF_8))
            + "."
            + base64.encodeToString(claims.toString().getBytes(StandardCharsets.UTF_8));
    try {
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initSign(privateKey);
      signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
      return signingInput + "." + base64.encodeToString(signature.sign());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Signing the assertion failed", e);
    }
  }

  private static String requireString(JsonObject file, String name) {
    if (!file.has(name) || !file.get(name).isJsonPrimitive()) {
      throw new IllegalArgumentException("The key file has no " + name);
    }
    String value = file.get(name).getAsString();
    if (value.isEmpty()) {
      throw new IllegalArgumentException("The key file's " + name + " is empty");
    }
    return value;
  }

  private static PrivateKey parsePrivateKey(String pem) {
    String begin = "-----BEGIN PRIVATE KEY-----";
    String end = "-----END PRIVATE KEY-----";
    int from = pem.indexOf(begin);
    int to = pem.indexOf(end);
    if (from < 0 || to < from) {
      throw new IllegalArgumentException("The key file's private_key isn't a PKCS #8 PEM key");
    }
    String body = pem.substring(from + begin.length(), to).replaceAll("\\s", "");
    try {
      byte[] der = Base64.getDecoder().decode(body);
      return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    } catch (IllegalArgumentException | GeneralSecurityException e) {
      throw new IllegalArgumentException("The key file's private_key isn't an RSA key", e);
    }
  }
}
