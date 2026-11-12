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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** setOrgId and setCredentials are required, so a client never starts half set up. */
class UBIKOptionsTest {

  @Test
  void buildsAClient() {
    ApiKeyCredentials credentials = TestKeys.credentials();
    UBIKOptions options =
        UBIKOptions.newBuilder().setOrgId(TestKeys.ORG_ID).setCredentials(credentials).build();
    assertEquals(TestKeys.ORG_ID, options.getOrgId());
    assertSame(credentials, options.getCredentials());
    assertEquals("https://ubik.io", options.getHost());
    assertSame(options, options.getService().getOptions());
  }

  @Test
  void requiresTheOrgId() {
    UBIKOptions.Builder builder = UBIKOptions.newBuilder().setCredentials(TestKeys.credentials());
    IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
    assertEquals("setOrgId is required", e.getMessage());
  }

  @Test
  void requiresAnOrgIdThatIsntEmpty() {
    UBIKOptions.Builder builder =
        UBIKOptions.newBuilder().setOrgId("").setCredentials(TestKeys.credentials());
    IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
    assertEquals("setOrgId is required", e.getMessage());
  }

  @Test
  void requiresTheCredentials() {
    UBIKOptions.Builder builder = UBIKOptions.newBuilder().setOrgId(TestKeys.ORG_ID);
    IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
    assertEquals("setCredentials is required", e.getMessage());
  }

  @Test
  void hostLosesItsTrailingSlash() {
    UBIKOptions options =
        UBIKOptions.newBuilder()
            .setOrgId(TestKeys.ORG_ID)
            .setCredentials(TestKeys.credentials())
            .setHost("https://ubik.example/")
            .build();
    assertEquals("https://ubik.example", options.getHost());
  }

  @Test
  void hostNeedsAScheme() {
    assertThrows(IllegalArgumentException.class, () -> UBIKOptions.newBuilder().setHost("ubik.io"));
  }
}
