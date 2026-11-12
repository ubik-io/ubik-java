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

package io.ubik.webhook;

import com.google.gson.annotations.SerializedName;

/** What a cdr-add event is about: the completed call's CDR. */
public final class CdrAddEvent {

  /** The cdr id of a test event, sent from the webhook's page: it has no CDR. */
  public static final String TEST_CDR_ID = "00000000-0000-0000-0000-000000000000";

  @SerializedName("id")
  private String cdrId;

  private CdrAddEvent() {}

  /**
   * The CDR to read with {@code client.cdrs().get(id)}. A test event has
   * {@value #TEST_CDR_ID} (no CDR).
   */
  public String getCdrId() {
    return cdrId;
  }
}
