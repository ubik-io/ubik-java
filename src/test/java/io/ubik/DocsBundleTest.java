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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Writes target/docs: docs-manifest.json and the example files, the bundle
 * the release attaches for the developer docs. The manifest comes from this
 * build (its version, the examples ExamplesTest runs), so the docs can't
 * show a version or a snippet that wasn't built and tested.
 */
class DocsBundleTest {

  private static final Path EXAMPLES = Path.of("src/examples/java");
  private static final Path BUNDLE = Path.of("target/docs");

  @Test
  void writesTheDocsBundle() throws IOException {
    String version = Version.VERSION;
    assertTrue(!version.equals("unknown"), "the build's version");

    // Each region id → its file, relative to the bundle.
    Map<String, String> snippets = new TreeMap<>();
    Pattern start = Pattern.compile("// \\[START ([a-z-]+)\\]");
    try (Stream<Path> files = Files.walk(EXAMPLES)) {
      for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
        Path relative = EXAMPLES.relativize(file);
        Path target = BUNDLE.resolve("examples").resolve(relative);
        Files.createDirectories(target.getParent());
        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
        Matcher m = start.matcher(Files.readString(file));
        while (m.find()) {
          String previous = snippets.put(m.group(1), "examples/" + relative.toString().replace('\\', '/'));
          assertTrue(previous == null, m.group(1) + " is in two files");
        }
      }
    }

    JsonObject manifest = new JsonObject();
    manifest.addProperty("groupId", "io.ubik");
    manifest.addProperty("artifactId", "ubik-java");
    manifest.addProperty("version", version);
    manifest.addProperty("tag", "v" + version);
    manifest.addProperty("commit", System.getenv().getOrDefault("GITHUB_SHA", ""));
    manifest.addProperty("java", "11");
    manifest.addProperty(
        "maven",
        "<dependency>\n  <groupId>io.ubik</groupId>\n  <artifactId>ubik-java</artifactId>\n  <version>"
            + version + "</version>\n</dependency>");
    manifest.addProperty("gradle", "implementation(\"io.ubik:ubik-java:" + version + "\")");
    JsonObject entries = new JsonObject();
    snippets.forEach(
        (id, file) -> {
          JsonObject entry = new JsonObject();
          entry.addProperty("file", file);
          entry.addProperty("region", id);
          entries.add(id, entry);
        });
    manifest.add("snippets", entries);

    Files.createDirectories(BUNDLE);
    Files.writeString(
        BUNDLE.resolve("docs-manifest.json"),
        new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(manifest) + "\n",
        StandardCharsets.UTF_8);

    // Every snippet the docs cite is in the bundle, and its file is there.
    assertEquals(new java.util.TreeSet<>(ExamplesTest.DOCS_IDS), new java.util.TreeSet<>(snippets.keySet()));
    for (String file : snippets.values()) {
      assertTrue(Files.exists(BUNDLE.resolve(file)), file);
    }
  }
}
