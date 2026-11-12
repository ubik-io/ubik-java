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
import java.util.Map;

/**
 * One step of the call.
 */
public final class TimelineEntry {

  @SerializedName("id")
  private String id;
  @SerializedName("parent_id")
  private String parentId;
  @SerializedName("type")
  private String type;
  @SerializedName("start_timestamp_milli")
  private long startTimestampMilli;
  @SerializedName("end_timestamp_milli")
  private long endTimestampMilli;
  @SerializedName("outcome")
  private String outcome;
  @SerializedName("resource")
  private TimelineResource resource;
  @SerializedName("members")
  private List<TimelineResource> members;
  @SerializedName("attributes")
  private Map<String, Object> attributes;

  private TimelineEntry() {}

  /**
   * The step's id.
   */
  public String getId() {
    return id;
  }

  /**
   * The step this one is part of; absent for the first step.
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * What the step is. Other values can appear.
   * <ul>
   *   <li>INIT: the call starts.
   *   <li>FROM_SIP_PHONE: from one of your phones.
   *   <li>FROM_SIP_PSTN: from an outside number.
   *   <li>ROUTE_EXTENSION: routed to an extension.
   *   <li>ROUTE_PSTN: routed to an outside number.
   *   <li>RING_GROUP: a group of phones rings.
   *   <li>RING_PHONE: one phone rings.
   *   <li>TO_SIP_PSTN: sent to an outside number.
   *   <li>IVR: an automated attendant.
   *   <li>VOICEMAIL: the voicemail.
   *   <li>ANNOUNCEMENT: an announcement is played.
   *   <li>RECORDING_START: a recording starts.
   *   <li>RECORDING_STOP: a recording stops.
   * </ul>
   */
  public String getType() {
    return type;
  }

  /**
   * When the step started, in Unix milliseconds.
   */
  public long getStartTimestampMilli() {
    return startTimestampMilli;
  }

  /**
   * When the step ended, in Unix milliseconds.
   */
  public long getEndTimestampMilli() {
    return endTimestampMilli;
  }

  /**
   * How the step ended. Other values can appear.
   * <ul>
   *   <li>ANSWERED: the step answered the call.
   * </ul>
   */
  public String getOutcome() {
    return outcome;
  }

  /**
   * The resource.
   * <p>Null when absent.
   */
  public TimelineResource getResource() {
    return resource;
  }

  /**
   * A ring group's phones, all of them, whether they rang or not.
   * <p>Empty when absent.
   */
  public List<TimelineResource> getMembers() {
    return members == null ? Collections.emptyList() : Collections.unmodifiableList(members);
  }

  /**
   * Facts about the step, such as the caller ID shown or the keys
   * <p>pressed. They depend on the step type; new ones can appear.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getAttributes() {
    return attributes == null ? Collections.emptyMap() : Collections.unmodifiableMap(attributes);
  }
}
