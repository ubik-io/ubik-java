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
 * Snapshot of a PHONE. Other fields can appear.
 */
public final class PhoneSnapshot {

  @SerializedName("id")
  private String id;
  @SerializedName("org_id")
  private String orgId;
  @SerializedName("creation_timestamp_milli")
  private long creationTimestampMilli;
  @SerializedName("description")
  private String description;
  @SerializedName("type")
  private String type;
  @SerializedName("address")
  private SnapshotAddress address;
  @SerializedName("internal_caller_id")
  private SnapshotCallerId internalCallerId;
  @SerializedName("external_caller_id")
  private SnapshotCallerId externalCallerId;
  @SerializedName("music_on_hold_id")
  private String musicOnHoldId;
  @SerializedName("call_recording_settings")
  private Map<String, Object> callRecordingSettings;
  @SerializedName("announcement_settings")
  private Map<String, Object> announcementSettings;
  @SerializedName("user")
  private Map<String, Object> user;

  private PhoneSnapshot() {}

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
   * Its name.
   */
  public String getDescription() {
    return description;
  }

  /**
   * <ul>
   *   <li>SIP: a SIP phone.
   * </ul>
   */
  public String getType() {
    return type;
  }

  /**
   * The phone's emergency address during the call: the address sent with a 911 call from this phone. Kept for audits.
   * <p>Null when absent.
   */
  public SnapshotAddress getAddress() {
    return address;
  }

  /**
   * The caller ID your users saw.
   * <p>Null when absent.
   */
  public SnapshotCallerId getInternalCallerId() {
    return internalCallerId;
  }

  /**
   * The caller ID people outside saw.
   * <p>Null when absent.
   */
  public SnapshotCallerId getExternalCallerId() {
    return externalCallerId;
  }

  /**
   * Its music on hold; "" for the default one.
   */
  public String getMusicOnHoldId() {
    return musicOnHoldId;
  }

  /**
   * The phone's automatic recording switches. A call is recorded only when the switch for its direction is on and the phone's user has the call recording add-on.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getCallRecordingSettings() {
    return callRecordingSettings == null ? Collections.emptyMap() : Collections.unmodifiableMap(callRecordingSettings);
  }

  /**
   * What the person called hears when the recording of one of the phone's outgoing calls starts.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getAnnouncementSettings() {
    return announcementSettings == null ? Collections.emptyMap() : Collections.unmodifiableMap(announcementSettings);
  }

  /**
   * Its user during the call.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getUser() {
    return user == null ? Collections.emptyMap() : Collections.unmodifiableMap(user);
  }
}
