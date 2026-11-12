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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A local HTTP server that records requests and answers with a set response. */
final class StubServer implements AutoCloseable {

  /** One request the server received. */
  static final class Request {
    final String method;
    final String path;
    final String query;
    final String authorization;
    final String contentType;
    final String userAgent;
    final String body;

    Request(
        String method,
        String path,
        String query,
        String authorization,
        String contentType,
        String userAgent,
        String body) {
      this.method = method;
      this.path = path;
      this.query = query;
      this.authorization = authorization;
      this.contentType = contentType;
      this.userAgent = userAgent;
      this.body = body;
    }
  }

  private static final class Response {
    final int status;
    final String contentType;
    final byte[] body;

    Response(int status, String contentType, byte[] body) {
      this.status = status;
      this.contentType = contentType;
      this.body = body;
    }
  }

  private final Map<String, Deque<Response>> routes = new HashMap<>();
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
                    exchange.getRequestURI().getRawQuery(),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    exchange.getRequestHeaders().getFirst("User-Agent"),
                    requestBody));
          }
          Response response = null;
          synchronized (requests) {
            Deque<Response> queue = routes.get(exchange.getRequestURI().getPath());
            if (queue != null && !queue.isEmpty()) {
              response = queue.size() > 1 ? queue.poll() : queue.peek();
            }
          }
          if (response == null) {
            response = new Response(status, contentType, body.getBytes(StandardCharsets.UTF_8));
          }
          byte[] bytes = response.body;
          exchange.getResponseHeaders().set("Content-Type", response.contentType);
          exchange.sendResponseHeaders(response.status, bytes.length == 0 ? -1 : bytes.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
          }
        });
    server.start();
  }

  /**
   * Requests to this path get these answers in turn; the last one repeats.
   * Other paths get the answer set with {@link #respond(int, String, String)}.
   */
  void route(String path, int status, String contentType, String body) {
    route(path, status, contentType, body.getBytes(StandardCharsets.UTF_8));
  }

  void route(String path, int status, String contentType, byte[] body) {
    synchronized (requests) {
      routes.computeIfAbsent(path, p -> new ArrayDeque<>()).add(new Response(status, contentType, body));
    }
  }

  /** Requests to this path, in order. */
  List<Request> requests(String path) {
    List<Request> out = new ArrayList<>();
    for (Request request : requests()) {
      if (request.path.equals(path)) {
        out.add(request);
      }
    }
    return out;
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
