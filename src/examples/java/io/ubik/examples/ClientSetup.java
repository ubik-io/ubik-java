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

package io.ubik.examples;

import io.ubik.ApiKeyCredentials;
import io.ubik.UBIK;
import io.ubik.UBIKOptions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Create a client from an API key file. */
public final class ClientSetup {

  private ClientSetup() {}

  /** @param keyFile the API key file, for example ubik-api-key.json */
  public static UBIK create(Path keyFile) throws IOException {
    // [START client-setup]
    ApiKeyCredentials credentials;
    try (InputStream in = Files.newInputStream(keyFile)) {
      credentials = ApiKeyCredentials.fromStream(in);
    }

    UBIK client = UBIKOptions.newBuilder()
        .setOrgId("7d1f3a5c-2b4e-4f6a-8c9d-0e1f2a3b4c5d")
        .setCredentials(credentials)
        .build()
        .getService();
    // [END client-setup]
    return client;
  }
}
