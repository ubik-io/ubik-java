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
 * One side of a caller ID.
 */
public final class CallerId {

  @SerializedName("name")
  private String name;
  @SerializedName("number")
  private String number;
  @SerializedName("anonymous")
  private boolean anonymous;

  private CallerId() {}

  /**
   * The name; "" when there is none.
   */
  public String getName() {
    return name;
  }

  /**
   * The number; "" when there is none.
   */
  public String getNumber() {
    return number;
  }

  /**
   * True when this caller ID was hidden:
   * <ul>
   *   <li>name is then "Anonymous".
   *   <li>number is then "anonymous", except on TO_SIP_PSTN, where it stays the number dialed.
   * </ul>
   */
  public boolean getAnonymous() {
    return anonymous;
  }
}
