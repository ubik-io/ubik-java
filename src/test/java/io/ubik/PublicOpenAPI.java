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

  private final OpenApiInteractionValidator validator;
  private final OpenAPI openAPI;

  private PublicOpenAPI(String resource) {
    String yaml = read(resource);
    this.validator = OpenApiInteractionValidator.createForInlineApiSpecification(yaml).build();
    this.openAPI = new OpenAPIV3Parser().readContents(yaml).getOpenAPI();
  }

  /** Fails when the request doesn't follow the file. */
  void assertValidRequest(String method, String path, String contentType, String body) {
    SimpleRequest.Builder builder =
        new SimpleRequest.Builder(method, path).withContentType(contentType);
    if (body != null) {
      builder.withBody(body);
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
    MediaType media =
        openAPI.getPaths().get(path).getPost().getResponses().get(status).getContent()
            .get("application/json");
    Object example = name == null ? media.getExample() : media.getExamples().get(name).getValue();
    if (example == null) {
      throw new AssertionError("No example " + name + " for POST " + path + " " + status);
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
