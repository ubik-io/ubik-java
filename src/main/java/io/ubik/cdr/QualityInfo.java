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
 * Whether the call has audio quality data, shown in UBIK.
 */
public final class QualityInfo {

  @SerializedName("available")
  private boolean available;
  @SerializedName("expires_at_milli")
  private long expiresAtMilli;

  private QualityInfo() {}

  /**
   * True when there is quality data.
   */
  public boolean getAvailable() {
    return available;
  }

  /**
   * When the quality data is deleted, in Unix milliseconds.
   */
  public long getExpiresAtMilli() {
    return expiresAtMilli;
  }
}
