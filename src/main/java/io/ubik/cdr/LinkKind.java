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

/** A LinkKind value of the API. New values can appear: they read as UNRECOGNIZED. */
public enum LinkKind {
  /** ATTENDED_TRANSFER. */
  ATTENDED_TRANSFER("ATTENDED_TRANSFER"),
  /** A value this library doesn't know yet. */
  UNRECOGNIZED(null);

  private final String value;

  LinkKind(String value) {
    this.value = value;
  }

  /** The value on the wire; null for UNRECOGNIZED. */
  public String getValue() {
    return value;
  }

  /**
   * The constant for a wire value.
   *
   * @return UNRECOGNIZED when the value is unknown, null when it is null
   */
  public static LinkKind fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (LinkKind e : values()) {
      if (value.equals(e.value)) {
        return e;
      }
    }
    return UNRECOGNIZED;
  }
}
