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
 * Snapshot of a PHONE_NUMBER. Other fields can appear.
 */
public final class PhoneNumberSnapshot {

  @SerializedName("id")
  private String id;
  @SerializedName("org_id")
  private String orgId;
  @SerializedName("creation_timestamp_milli")
  private long creationTimestampMilli;
  @SerializedName("phone_number_e164")
  private String phoneNumberE164;
  @SerializedName("status")
  private String status;
  @SerializedName("order_id")
  private String orderId;
  @SerializedName("emergency_address")
  private SnapshotAddress emergencyAddress;
  @SerializedName("general_settings")
  private Map<String, Object> generalSettings;
  @SerializedName("business_hours")
  private SnapshotBusinessHours businessHours;
  @SerializedName("incoming_calls_open")
  private SnapshotPhoneNumberRoute incomingCallsOpen;
  @SerializedName("incoming_calls_closed")
  private SnapshotPhoneNumberRoute incomingCallsClosed;
  @SerializedName("holidays")
  private Map<String, Object> holidays;

  private PhoneNumberSnapshot() {}

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
   * The number, E.164.
   */
  public String getPhoneNumberE164() {
    return phoneNumberE164;
  }

  /**
   * <ul>
   *   <li>ACTIVE: in service.
   *   <li>PENDING_ACTIVATION: being activated.
   * </ul>
   */
  public String getStatus() {
    return status;
  }

  /**
   * The order that brought the number.
   */
  public String getOrderId() {
    return orderId;
  }

  /**
   * The number's emergency address during the call.
   * <p>Null when absent.
   */
  public SnapshotAddress getEmergencyAddress() {
    return emergencyAddress;
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
   * Where calls go during open hours.
   * <p>Null when absent.
   */
  public SnapshotPhoneNumberRoute getIncomingCallsOpen() {
    return incomingCallsOpen;
  }

  /**
   * Where calls go outside open hours.
   * <p>Null when absent.
   */
  public SnapshotPhoneNumberRoute getIncomingCallsClosed() {
    return incomingCallsClosed;
  }

  /**
   * Holidays.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getHolidays() {
    return holidays == null ? Collections.emptyMap() : Collections.unmodifiableMap(holidays);
  }
}
