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
import java.util.Map;

/**
 * Snapshot of an EXTENSION. Other fields can appear.
 */
public final class ExtensionSnapshot {

  @SerializedName("id")
  private String id;
  @SerializedName("org_id")
  private String orgId;
  @SerializedName("creation_timestamp_milli")
  private long creationTimestampMilli;
  @SerializedName("number")
  private long number;
  @SerializedName("description")
  private String description;
  @SerializedName("general_settings")
  private Map<String, Object> generalSettings;
  @SerializedName("business_hours")
  private SnapshotBusinessHours businessHours;
  @SerializedName("incoming_calls_open")
  private SnapshotExtensionCalls incomingCallsOpen;
  @SerializedName("incoming_calls_closed")
  private SnapshotExtensionCalls incomingCallsClosed;
  @SerializedName("holidays")
  private Map<String, Object> holidays;
  @SerializedName("voicemail_settings")
  private Map<String, Object> voicemailSettings;
  @SerializedName("announcement_settings")
  private Map<String, Object> announcementSettings;

  private ExtensionSnapshot() {}

  /**
   * Its id.
   */
  public String getId() {
    return id;
  }

  /**
   * Your organization's id.
   */
  public String getOrgId() {
    return orgId;
  }

  /**
   * When it was created, in Unix milliseconds.
   */
  public long getCreationTimestampMilli() {
    return creationTimestampMilli;
  }

  /**
   * The extension number.
   */
  public long getNumber() {
    return number;
  }

  /**
   * Its name.
   */
  public String getDescription() {
    return description;
  }

  /**
   * General settings.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getGeneralSettings() {
    return generalSettings == null ? Collections.emptyMap() : Collections.unmodifiableMap(generalSettings);
  }

  /**
   * The open hours, per day, in the resource's timezone.
   * <p>Null when absent.
   */
  public SnapshotBusinessHours getBusinessHours() {
    return businessHours;
  }

  /**
   * What happens to calls during open hours.
   * <p>Null when absent.
   */
  public SnapshotExtensionCalls getIncomingCallsOpen() {
    return incomingCallsOpen;
  }

  /**
   * What happens to calls outside open hours.
   * <p>Null when absent.
   */
  public SnapshotExtensionCalls getIncomingCallsClosed() {
    return incomingCallsClosed;
  }

  /**
   * Holidays.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getHolidays() {
    return holidays == null ? Collections.emptyMap() : Collections.unmodifiableMap(holidays);
  }

  /**
   * Its voicemail.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getVoicemailSettings() {
    return voicemailSettings == null ? Collections.emptyMap() : Collections.unmodifiableMap(voicemailSettings);
  }

  /**
   * What a caller hears before the extension's phones ring.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getAnnouncementSettings() {
    return announcementSettings == null ? Collections.emptyMap() : Collections.unmodifiableMap(announcementSettings);
  }
}
