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

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** A local HTTP server that records requests and answers with a set response. */
final class StubServer implements AutoCloseable {

  /** One request the server received. */
  static final class Request {
    final String method;
    final String path;
    final String contentType;
    final String userAgent;
    final String body;

    Request(String method, String path, String contentType, String userAgent, String body) {
      this.method = method;
      this.path = path;
      this.contentType = contentType;
      this.userAgent = userAgent;
      this.body = body;
    }
  }

  private final HttpServer server;
  private final List<Request> requests = new ArrayList<>();
  private volatile int status = 200;
  private volatile String contentType = "application/json";
  private volatile String body = "{}";

  StubServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext(
        "/",
        exchange -> {
          String requestBody =
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          synchronized (requests) {
            requests.add(
                new Request(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    exchange.getRequestHeaders().getFirst("User-Agent"),
                    requestBody));
          }
          byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", contentType);
          exchange.sendResponseHeaders(status, bytes.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
          }
        });
    server.start();
  }

  /** Every next request gets this answer. */
  void respond(int status, String contentType, String body) {
    this.status = status;
    this.contentType = contentType;
    this.body = body;
  }

  String host() {
    return "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort();
  }

  List<Request> requests() {
    synchronized (requests) {
      return new ArrayList<>(requests);
    }
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
