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
 * A time range.
 */
public final class SnapshotTimeRange {

  @SerializedName("start_hours")
  private long startHours;
  @SerializedName("start_minutes")
  private long startMinutes;
  @SerializedName("end_hours")
  private long endHours;
  @SerializedName("end_minutes")
  private long endMinutes;

  private SnapshotTimeRange() {}

  /**
   * Start hour, 0 to 23.
   */
  public long getStartHours() {
    return startHours;
  }

  /**
   * Start minute, 0 to 59.
   */
  public long getStartMinutes() {
    return startMinutes;
  }

  /**
   * End hour, 0 to 23.
   */
  public long getEndHours() {
    return endHours;
  }

  /**
   * End minute, 0 to 59.
   */
  public long getEndMinutes() {
    return endMinutes;
  }
}
