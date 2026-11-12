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
 * One recording of the call.
 */
public final class RecordingItem {

  @SerializedName("recording_session_id")
  private String recordingSessionId;
  @SerializedName("start_timestamp_milli")
  private long startTimestampMilli;
  @SerializedName("stop_timestamp_milli")
  private long stopTimestampMilli;
  @SerializedName("start_reason")
  private String startReason;
  @SerializedName("stop_reason")
  private String stopReason;
  @SerializedName("phone_list")
  private List<RecordingPhone> phoneList;
  @SerializedName("state")
  private String state;
  @SerializedName("mixdown_duration_milli")
  private long mixdownDurationMilli;
  @SerializedName("mixdown_file_id")
  private String mixdownFileId;
  @SerializedName("channel_list")
  private List<RecordingChannel> channelList;

  private RecordingItem() {}

  /**
   * The recording's id.
   */
  public String getRecordingSessionId() {
    return recordingSessionId;
  }

  /**
   * When the recording started, in Unix milliseconds.
   */
  public long getStartTimestampMilli() {
    return startTimestampMilli;
  }

  /**
   * When the recording stopped, in Unix milliseconds.
   */
  public long getStopTimestampMilli() {
    return stopTimestampMilli;
  }

  /**
   * Why it started. Other values can appear.
   * <ul>
   *   <li>AUTOMATIC: the phone records its calls automatically.
   *   <li>ON_DEMAND: someone pressed *70.
   * </ul>
   */
  public String getStartReason() {
    return startReason;
  }

  /**
   * Why it stopped. Other values can appear.
   * <ul>
   *   <li>ON_DEMAND: someone pressed *71.
   *   <li>CALL_END: the call ended.
   * </ul>
   */
  public String getStopReason() {
    return stopReason;
  }

  /**
   * The recorded phones and their users.
   * <p>Empty when absent.
   */
  public List<RecordingPhone> getPhoneList() {
    return phoneList == null ? Collections.emptyList() : Collections.unmodifiableList(phoneList);
  }

  /**
   * The state.
   * <ul>
   *   <li>PROCESSED: the files are ready to download.
   *   <li>IN_PROGRESS: still recording; read the CDR again later.
   *   <li>RECORDING_COMPLETE: the files are being prepared; read the CDR again in a few minutes.
   *   <li>FAILED_TOO_MANY_RETRIES: no file will be made.
   *   <li>EXPIRED: the files were deleted.
   * </ul>
   * <p>RecordingState.UNRECOGNIZED for a value this library doesn't know; getStateValue() gives it.
   */
  public RecordingState getState() {
    return RecordingState.fromValue(state);
  }

  /** The state as the API sent it. */
  public String getStateValue() {
    return state;
  }

  /**
   * The mixdown's duration, in milliseconds.
   */
  public long getMixdownDurationMilli() {
    return mixdownDurationMilli;
  }

  /**
   * The mixdown's file id when PROCESSED; empty before.
   */
  public String getMixdownFileId() {
    return mixdownFileId;
  }

  /**
   * One channel file per party, then the music on hold; empty until PROCESSED.
   * <p>Empty when absent.
   */
  public List<RecordingChannel> getChannelList() {
    return channelList == null ? Collections.emptyList() : Collections.unmodifiableList(channelList);
  }
}
