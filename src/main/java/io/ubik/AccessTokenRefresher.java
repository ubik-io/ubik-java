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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gets access tokens with an API key's JWT-bearer assertions, keeps one per
 * scope, and gets a new one before each expires.
 */
final class AccessTokenRefresher {

  static final String TOKEN_PATH = "/api/v1/auth/oauth/token";
  static final String JWT_BEARER = "urn:ietf:params:oauth:grant-type:jwt-bearer";

  /** A token is renewed this long before it expires. */
  static final Duration RENEW_BEFORE_EXPIRY = Duration.ofSeconds(60);

  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private final ApiKeyCredentials credentials;
  private final String tokenUrl;
  private final HttpClient httpClient;
  private final Clock clock;
  private final Map<String, AccessToken> tokens = new HashMap<>();

  AccessTokenRefresher(
      ApiKeyCredentials credentials, String host, HttpClient httpClient, Clock clock) {
    this.credentials = credentials;
    this.tokenUrl = host + TOKEN_PATH;
    this.httpClient = httpClient;
    this.clock = clock;
  }

  /** An access token holding this scope, from the cache when still valid. */
  synchronized String getAccessToken(String scope) {
    Instant now = clock.instant();
    AccessToken token = tokens.get(scope);
    if (token == null || !now.isBefore(token.renewAt)) {
      token = requestToken(scope, now);
      tokens.put(scope, token);
    }
    return token.value;
  }

  /** Forgets this scope's token, after the API refused it. */
  synchronized void invalidate(String scope) {
    tokens.remove(scope);
  }

  private AccessToken requestToken(String scope, Instant now) {
    String assertion = credentials.signAssertion(tokenUrl, now, UUID.randomUUID().toString());
    String body =
        "grant_type="
            + URLEncoder.encode(JWT_BEARER, StandardCharsets.UTF_8)
            + "&assertion="
            + URLEncoder.encode(assertion, StandardCharsets.UTF_8)
            + "&scope="
            + URLEncoder.encode(scope, StandardCharsets.UTF_8);
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(tokenUrl))
            .timeout(REQUEST_TIMEOUT)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "application/json")
            .header("User-Agent", Version.USER_AGENT)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      throw new UBIKException("Getting an access token failed: " + e.getMessage(), 0, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new UBIKException("Getting an access token was interrupted", 0, e);
    }

    JsonObject json = parseObject(response.body());
    if (response.statusCode() != 200) {
      throw new UBIKException(
          "Getting an access token failed: " + describeError(json, response.statusCode()),
          response.statusCode(),
          null);
    }
    String accessToken = getString(json, "access_token");
    long expiresIn = getLong(json, "expires_in");
    if (accessToken == null || accessToken.isEmpty() || expiresIn <= 0) {
      throw new UBIKException(
          "Getting an access token failed: the response has no access_token or expires_in",
          response.statusCode(),
          null);
    }
    Duration lifetime = Duration.ofSeconds(expiresIn);
    Duration renewBefore =
        lifetime.compareTo(RENEW_BEFORE_EXPIRY.multipliedBy(2)) < 0
            ? lifetime.dividedBy(2)
            : RENEW_BEFORE_EXPIRY;
    return new AccessToken(accessToken, now.plus(lifetime).minus(renewBefore));
  }

  private static String describeError(JsonObject json, int statusCode) {
    String error = getString(json, "error");
    String description = getString(json, "error_description");
    if (error == null) {
      return "HTTP " + statusCode;
    }
    return description == null || description.isEmpty() ? error : error + ": " + description;
  }

  private static JsonObject parseObject(String body) {
    try {
      JsonElement element = JsonParser.parseString(body);
      return element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
    } catch (JsonParseException e) {
      return new JsonObject();
    }
  }

  private static String getString(JsonObject json, String name) {
    JsonElement value = json.get(name);
    return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
  }

  private static long getLong(JsonObject json, String name) {
    JsonElement value = json.get(name);
    if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      return 0;
    }
    return value.getAsLong();
  }

  private static final class AccessToken {
    final String value;
    final Instant renewAt;

    AccessToken(String value, Instant renewAt) {
      this.value = value;
      this.renewAt = renewAt;
    }
  }
}
