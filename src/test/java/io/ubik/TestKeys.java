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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/** An invented API key, made once for all tests. */
final class TestKeys {

  static final String ORG_ID = "7d1f3a5c-2b4e-4f6a-8c9d-0e1f2a3b4c5d";
  static final String KEY_FILE_ID = "7c9d0e1f-3a4b-4c5d-8e6f-0a1b2c3d4e5f";
  static final String KEY_ID = "4b8e2f6a-1c3d-4e5f-9a0b-6c7d8e9f0a1b";

  static final KeyPair KEY_PAIR = generate();

  private TestKeys() {}

  static String keyFileJson() {
    String pem =
        "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getMimeEncoder(64, new byte[] {'\n'})
                .encodeToString(KEY_PAIR.getPrivate().getEncoded())
            + "\n-----END PRIVATE KEY-----\n";
    JsonObject file = new JsonObject();
    file.addProperty("id", KEY_FILE_ID);
    file.addProperty("key_id", KEY_ID);
    file.addProperty("private_key", pem);
    return file.toString();
  }

  static ApiKeyCredentials credentials() {
    try {
      return ApiKeyCredentials.fromStream(
          new ByteArrayInputStream(keyFileJson().getBytes(StandardCharsets.UTF_8)));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static KeyPair generate() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      return generator.generateKeyPair();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
