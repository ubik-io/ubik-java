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
import java.util.Collections;
import java.util.List;

/**
 * The open hours, per day, in the resource's timezone.
 */
public final class SnapshotBusinessHours {

  @SerializedName("monday")
  private List<SnapshotTimeRange> monday;
  @SerializedName("tuesday")
  private List<SnapshotTimeRange> tuesday;
  @SerializedName("wednesday")
  private List<SnapshotTimeRange> wednesday;
  @SerializedName("thursday")
  private List<SnapshotTimeRange> thursday;
  @SerializedName("friday")
  private List<SnapshotTimeRange> friday;
  @SerializedName("saturday")
  private List<SnapshotTimeRange> saturday;
  @SerializedName("sunday")
  private List<SnapshotTimeRange> sunday;

  private SnapshotBusinessHours() {}

  /**
   * Open ranges on Monday; empty or null when closed all day.
   * <p>Empty when absent.
   */
  public List<SnapshotTimeRange> getMonday() {
    return monday == null ? Collections.emptyList() : Collections.unmodifiableList(monday);
  }

  /**
   * Open ranges on Tuesday; empty or null when closed all day.
   * <p>Empty when absent.
   */
  public List<SnapshotTimeRange> getTuesday() {
    return tuesday == null ? Collections.emptyList() : Collections.unmodifiableList(tuesday);
  }

  /**
   * Open ranges on Wednesday; empty or null when closed all day.
   * <p>Empty when absent.
   */
  public List<SnapshotTimeRange> getWednesday() {
    return wednesday == null ? Collections.emptyList() : Collections.unmodifiableList(wednesday);
  }

  /**
   * Open ranges on Thursday; empty or null when closed all day.
   * <p>Empty when absent.
   */
  public List<SnapshotTimeRange> getThursday() {
    return thursday == null ? Collections.emptyList() : Collections.unmodifiableList(thursday);
  }

  /**
   * Open ranges on Friday; empty or null when closed all day.
   * <p>Empty when absent.
   */
  public List<SnapshotTimeRange> getFriday() {
    return friday == null ? Collections.emptyList() : Collections.unmodifiableList(friday);
  }

  /**
   * Open ranges on Saturday; empty or null when closed all day.
   * <p>Empty when absent.
   */
  public List<SnapshotTimeRange> getSaturday() {
    return saturday == null ? Collections.emptyList() : Collections.unmodifiableList(saturday);
  }

  /**
   * Open ranges on Sunday; empty or null when closed all day.
   * <p>Empty when absent.
   */
  public List<SnapshotTimeRange> getSunday() {
    return sunday == null ? Collections.emptyList() : Collections.unmodifiableList(sunday);
  }
}
