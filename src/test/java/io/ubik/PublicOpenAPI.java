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

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * A public OpenAPI file of the UBIK API, pinned in src/test/resources/openapi.
 * Requests the library sends are checked against it, and stubs answer with
 * its examples, so the tests hold the library to the published contract.
 */
final class PublicOpenAPI {

  /** The authentication file: UBIK API authentication. */
  static final PublicOpenAPI AUTH = new PublicOpenAPI("/openapi/auth.yaml");

  /** The CDR file: UBIK API call history. */
  static final PublicOpenAPI CDR = new PublicOpenAPI("/openapi/cdr.yaml");

  private final OpenApiInteractionValidator validator;
  private final OpenAPI openAPI;

  private PublicOpenAPI(String resource) {
    String yaml = read(resource);
    this.validator = OpenApiInteractionValidator.createForInlineApiSpecification(yaml).build();
    this.openAPI = new OpenAPIV3Parser().readContents(yaml).getOpenAPI();
  }

  /** Fails when the request doesn't follow the file. */
  void assertValidRequest(String method, String path, String contentType, String body) {
    assertValidRequest(method, path, null, contentType, body);
  }

  /** Fails when the request doesn't follow the file. query is raw, or null. */
  void assertValidRequest(String method, String path, String query, String contentType, String body) {
    SimpleRequest.Builder builder = new SimpleRequest.Builder(method, path);
    if (contentType != null) {
      builder.withContentType(contentType);
    }
    if (body != null) {
      builder.withBody(body);
    }
    if (query != null) {
      for (String pair : query.split("&")) {
        int equals = pair.indexOf('=');
        builder.withQueryParam(
            java.net.URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8),
            java.net.URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8));
      }
    }
    ValidationReport report = validator.validateRequest(builder.build());
    if (report.hasErrors()) {
      throw new AssertionError(
          method + " " + path + " breaks the public OpenAPI file:\n" + report.getMessages());
    }
  }

  /**
   * The example of a POST response, as JSON.
   *
   * @param name the example's name, or null for the single example
   */
  String postResponseExample(String path, String status, String name) {
    return responseExample(openAPI.getPaths().get(path).getPost(), status, "application/json", name);
  }

  /**
   * The example of a GET response: JSON, or the text itself for text/plain.
   *
   * @param name the example's name, or null for the single example
   */
  String getResponseExample(String path, String status, String contentType, String name) {
    return responseExample(openAPI.getPaths().get(path).getGet(), status, contentType, name);
  }

  /** The OpenAPI model, for tests that compare the library with the file. */
  OpenAPI model() {
    return openAPI;
  }

  private String responseExample(
      io.swagger.v3.oas.models.Operation operation, String status, String contentType, String name) {
    io.swagger.v3.oas.models.responses.ApiResponse response = operation.getResponses().get(status);
    if (response.get$ref() != null) {
      response =
          openAPI.getComponents().getResponses()
              .get(response.get$ref().substring(response.get$ref().lastIndexOf('/') + 1));
    }
    MediaType media = response.getContent().get(contentType);
    Object example = name == null ? media.getExample() : media.getExamples().get(name).getValue();
    if (example == null) {
      throw new AssertionError("No example " + name + " for " + status);
    }
    if (example instanceof String) {
      return (String) example;
    }
    try {
      return new ObjectMapper().writeValueAsString(example);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String read(String resource) {
    try (InputStream in = PublicOpenAPI.class.getResourceAsStream(resource)) {
      if (in == null) {
        throw new IllegalStateException("Missing " + resource);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
