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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** This library's version, from the build. */
final class Version {

  static final String VERSION = load();

  /** Sent with each request, so UBIK can tell which library versions are in use. */
  static final String USER_AGENT =
      "ubik-java/" + VERSION + " (Java " + System.getProperty("java.version") + ")";

  private Version() {}

  private static String load() {
    try (InputStream in = Version.class.getResourceAsStream("version.properties")) {
      if (in == null) {
        return "unknown";
      }
      Properties properties = new Properties();
      properties.load(in);
      return properties.getProperty("version", "unknown");
    } catch (IOException e) {
      return "unknown";
    }
  }
}
