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
 * One CDR, with the call's steps, the resources the call involved and its recordings.
 */
public final class CdrDetail {

  @SerializedName("id")
  private String id;
  @SerializedName("org_id")
  private String orgId;
  @SerializedName("direction")
  private String direction;
  @SerializedName("disposition")
  private String disposition;
  @SerializedName("src")
  private String src;
  @SerializedName("dst")
  private String dst;
  @SerializedName("start_timestamp_milli")
  private long startTimestampMilli;
  @SerializedName("end_timestamp_milli")
  private long endTimestampMilli;
  @SerializedName("duration_milli")
  private long durationMilli;
  @SerializedName("answered")
  private boolean answered;
  @SerializedName("answer_timestamp_milli")
  private long answerTimestampMilli;
  @SerializedName("billable_duration_milli")
  private long billableDurationMilli;
  @SerializedName("answered_by")
  private String answeredBy;
  @SerializedName("forwarded_to")
  private String forwardedTo;
  @SerializedName("created_timestamp_milli")
  private long createdTimestampMilli;
  @SerializedName("from")
  private ResourceRef from;
  @SerializedName("to")
  private ResourceRef to;
  @SerializedName("recording_session_count")
  private long recordingSessionCount;
  @SerializedName("timeline")
  private List<TimelineEntry> timeline;
  @SerializedName("hangup_by")
  private String hangupBy;
  @SerializedName("disposition_details")
  private String dispositionDetails;
  @SerializedName("links")
  private List<LinkItem> links;
  @SerializedName("resources")
  private List<ResourceRef> resources;
  @SerializedName("quality")
  private QualityInfo quality;
  @SerializedName("recording_session_list")
  private List<RecordingItem> recordingSessionList;

  private CdrDetail() {}

  /**
   * The CDR's id.
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
   * The direction.
   * <ul>
   *   <li>INBOUND_PSTN: from an outside number.
   *   <li>OUTBOUND_PSTN: to an outside number.
   *   <li>INTERNAL: between two of your organization's phones.
   *   <li>EMERGENCY: to an emergency number.
   *   <li>UNKNOWN: couldn't be determined.
   * </ul>
   * <p>Direction.UNRECOGNIZED for a value this library doesn't know; getDirectionValue() gives it.
   */
  public Direction getDirection() {
    return Direction.fromValue(direction);
  }

  /** The direction as the API sent it. */
  public String getDirectionValue() {
    return direction;
  }

  /**
   * How the call ended. Other values can appear.
   * <ul>
   *   <li>LAST_LEG_HUNGUP: ended normally; the last party hung up.
   *   <li>SOURCE_LEG_HUNGUP_BEFORE_SETUP_FINISHED: the caller hung up before the call was answered.
   *   <li>NO_ANSWER: nobody answered.
   *   <li>CALLEE_BUSY: the person called was busy.
   *   <li>DECLINE: the person called declined.
   *   <li>NOT_FOUND: the destination doesn't exist.
   *   <li>SERVICE_UNAVAILABLE: the destination was unavailable.
   *   <li>TRANSFER: the call was transferred.
   *   <li>BLIND_TRANSFER_FAILED: a transfer failed.
   *   <li>MEDIA_TIMEOUT: no audio for too long.
   *   <li>SETUP_ERROR: the call couldn't be set up.
   *   <li>SUSPENDED_ORG: the organization is suspended.
   *   <li>INACTIVE_ORG: the organization is inactive.
   * </ul>
   * <p>Disposition.UNRECOGNIZED for a value this library doesn't know; getDispositionValue() gives it.
   */
  public Disposition getDisposition() {
    return Disposition.fromValue(disposition);
  }

  /** The disposition as the API sent it. */
  public String getDispositionValue() {
    return disposition;
  }

  /**
   * The calling number or extension.
   */
  public String getSrc() {
    return src;
  }

  /**
   * The called number or extension.
   */
  public String getDst() {
    return dst;
  }

  /**
   * When the call started, in Unix milliseconds.
   */
  public long getStartTimestampMilli() {
    return startTimestampMilli;
  }

  /**
   * When the call ended, in Unix milliseconds.
   */
  public long getEndTimestampMilli() {
    return endTimestampMilli;
  }

  /**
   * The call's duration, in milliseconds.
   */
  public long getDurationMilli() {
    return durationMilli;
  }

  /**
   * True when the call was answered.
   */
  public boolean getAnswered() {
    return answered;
  }

  /**
   * When the call was answered, in Unix milliseconds; 0 when not answered.
   */
  public long getAnswerTimestampMilli() {
    return answerTimestampMilli;
  }

  /**
   * The billed duration, in milliseconds.
   */
  public long getBillableDurationMilli() {
    return billableDurationMilli;
  }

  /**
   * What answered when no person did; absent when a person answered.
   * <ul>
   *   <li>VOICEMAIL: the voicemail.
   *   <li>FORWARD: a forward to an outside number (forwarded_to).
   *   <li>IVR: an automated attendant.
   * </ul>
   * <p>AnsweredBy.UNRECOGNIZED for a value this library doesn't know; getAnsweredByValue() gives it.
   */
  public AnsweredBy getAnsweredBy() {
    return AnsweredBy.fromValue(answeredBy);
  }

  /** The answered_by as the API sent it. */
  public String getAnsweredByValue() {
    return answeredBy;
  }

  /**
   * The outside number a FORWARD sent the call to.
   */
  public String getForwardedTo() {
    return forwardedTo;
  }

  /**
   * When the CDR was created, in Unix milliseconds.
   */
  public long getCreatedTimestampMilli() {
    return createdTimestampMilli;
  }

  /**
   * Who the call was from, as it is now; absent when the call has none.
   * <p>Null when absent.
   */
  public ResourceRef getFrom() {
    return from;
  }

  /**
   * Who the call was for, as it is now; absent when the call has none.
   * <p>Null when absent.
   */
  public ResourceRef getTo() {
    return to;
  }

  /**
   * How many recordings the call has, in any state: as many as the CDR's
   * <p>recording_session_list. 0 when the call has none.
   */
  public long getRecordingSessionCount() {
    return recordingSessionCount;
  }

  /**
   * The call's steps, in a flat list; parent_id nests them.
   * <p>Empty when absent.
   */
  public List<TimelineEntry> getTimeline() {
    return timeline == null ? Collections.emptyList() : Collections.unmodifiableList(timeline);
  }

  /**
   * Who ended the call.
   * <ul>
   *   <li>CALLER: the caller.
   *   <li>CALLEE: the person called.
   *   <li>SYSTEM: UBIK.
   * </ul>
   * <p>HangupBy.UNRECOGNIZED for a value this library doesn't know; getHangupByValue() gives it.
   */
  public HangupBy getHangupBy() {
    return HangupBy.fromValue(hangupBy);
  }

  /** The hangup_by as the API sent it. */
  public String getHangupByValue() {
    return hangupBy;
  }

  /**
   * More detail on how the call ended, when there is some.
   */
  public String getDispositionDetails() {
    return dispositionDetails;
  }

  /**
   * CDRs linked to this one by an attended transfer.
   * <p>Empty when absent.
   */
  public List<LinkItem> getLinks() {
    return links == null ? Collections.emptyList() : Collections.unmodifiableList(links);
  }

  /**
   * Everything the call involved, as it is now.
   * <p>Empty when absent.
   */
  public List<ResourceRef> getResources() {
    return resources == null ? Collections.emptyList() : Collections.unmodifiableList(resources);
  }

  /**
   * Whether the call has audio quality data.
   * <p>Null when absent.
   */
  public QualityInfo getQuality() {
    return quality;
  }

  /**
   * The CDR's recordings.
   * <p>Empty when absent.
   */
  public List<RecordingItem> getRecordingSessionList() {
    return recordingSessionList == null ? Collections.emptyList() : Collections.unmodifiableList(recordingSessionList);
  }

}
