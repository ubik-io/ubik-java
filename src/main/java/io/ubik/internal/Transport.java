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

package io.ubik.internal;

import io.ubik.NotFoundException;
import io.ubik.PermissionDeniedException;
import io.ubik.UBIKException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Sends the requests of the UBIK API: the organization, the access token and
 * the error mapping. Internal: not part of this library's API.
 */
public final class Transport {

  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
  private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(10);

  private final HttpClient httpClient;
  private final String host;
  private final String orgId;
  private final TokenSource tokens;
  private final String userAgent;

  public Transport(
      HttpClient httpClient, String host, String orgId, TokenSource tokens, String userAgent) {
    this.httpClient = httpClient;
    this.host = host;
    this.orgId = orgId;
    this.tokens = tokens;
    this.userAgent = userAgent;
  }

  /** The organization every request works in. */
  public String getOrgId() {
    return orgId;
  }

  /** GET path?org_id=…, with a token holding scope. Any status comes back. */
  public HttpResponse<String> get(String path, String scope) {
    return send(scope, () -> newRequest(path + "?org_id=" + encode(orgId)).GET());
  }

  /** POST path with a JSON body, with a token holding scope. Any status comes back. */
  public HttpResponse<String> post(String path, String json, String scope) {
    return send(
        scope,
        () ->
            newRequest(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)));
  }

  /**
   * Writes the file at a signed storage link to out. The link holds its own
   * signature: the UBIK access token is never sent to it.
   */
  public void download(String url, OutputStream out) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(DOWNLOAD_TIMEOUT)
            .header("User-Agent", userAgent)
            .GET()
            .build();
    HttpResponse<InputStream> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
    } catch (IOException e) {
      throw new UBIKException("Downloading the file failed: " + e.getMessage(), 0, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new UBIKException("Downloading the file was interrupted", 0, e);
    }
    try (InputStream in = response.body()) {
      if (response.statusCode() != 200) {
        throw new UBIKException(
            "Downloading the file failed: HTTP " + response.statusCode(),
            response.statusCode(),
            null);
      }
      in.transferTo(out);
    } catch (IOException e) {
      throw new UBIKException("Downloading the file failed: " + e.getMessage(), 0, e);
    }
  }

  /**
   * Throws the exception for a response that isn't 200.
   *
   * @throws NotFoundException for 404
   * @throws PermissionDeniedException for 403
   * @throws UBIKException for any other status
   */
  public static void check(HttpResponse<String> response) {
    int status = response.statusCode();
    if (status == 200) {
      return;
    }
    String body = response.body() == null ? "" : response.body().trim();
    String message = body.isEmpty() ? "HTTP " + status : "HTTP " + status + ": " + body;
    switch (status) {
      case 403:
        throw new PermissionDeniedException(message);
      case 404:
        throw new NotFoundException(message);
      default:
        throw new UBIKException(message, status, null);
    }
  }

  /** Encodes one path segment or query value. */
  public static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private HttpRequest.Builder newRequest(String pathAndQuery) {
    return HttpRequest.newBuilder(URI.create(host + pathAndQuery))
        .timeout(REQUEST_TIMEOUT)
        .header("Accept", "application/json")
        .header("User-Agent", userAgent);
  }

  private interface RequestFactory {
    HttpRequest.Builder create();
  }

  /** Sends once, and once more with a new token when the API refuses the token. */
  private HttpResponse<String> send(String scope, RequestFactory factory) {
    HttpResponse<String> response = sendOnce(scope, factory);
    if (response.statusCode() == 401) {
      tokens.invalidate(scope);
      response = sendOnce(scope, factory);
    }
    return response;
  }

  private HttpResponse<String> sendOnce(String scope, RequestFactory factory) {
    HttpRequest request =
        factory.create().header("Authorization", "Bearer " + tokens.getAccessToken(scope)).build();
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UBIKException("The request failed: " + e.getMessage(), 0, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new UBIKException("The request was interrupted", 0, e);
    }
  }
}
