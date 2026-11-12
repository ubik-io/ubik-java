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

package io.ubik.cdr;

import com.google.gson.annotations.SerializedName;

/**
 * The caller ID of the party on a channel, as presented on the call:
 * <ul>
 *   <li>FROM_SIP_PHONE or RING_PHONE: the phone's own caller ID, internal and external.
 *   <li>FROM_SIP_PSTN: what the outside caller presented, external only.
 *   <li>TO_SIP_PSTN: the number dialed and the name the other end gave, external only.
 *   <li>A channel without a party: both null.
 * </ul>
 */
public final class SessionCallerId {

  @SerializedName("internal")
  private CallerId internal;
  @SerializedName("external")
  private CallerId external;

  private SessionCallerId() {}

  /**
   * What the org's users see; null for a party outside the org.
   */
  public CallerId getInternal() {
    return internal;
  }

  /**
   * What people outside your organization see:
   * <ul>
   *   <li>One of your phones: always an object, with "" when unknown.
   *   <li>An outside party that presented nothing: null.
   * </ul>
   */
  public CallerId getExternal() {
    return external;
  }
}
