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
 * One file of a recording, and who is on it. Every field is always
 * <p>sent: "" when a text is empty, null when an object is absent.
 */
public final class RecordingChannel {

  @SerializedName("channel_id")
  private String channelId;
  @SerializedName("file_id")
  private String fileId;
  @SerializedName("type")
  private String type;
  @SerializedName("side")
  private String side;
  @SerializedName("via")
  private String via;
  @SerializedName("resource")
  private TimelineResource resource;
  @SerializedName("caller_id")
  private SessionCallerId callerId;
  @SerializedName("phone")
  private RecordingChannelPhone phone;
  @SerializedName("user")
  private RecordingChannelUser user;
  @SerializedName("extension")
  private ExtensionView extension;

  private RecordingChannel() {}

  /**
   * The channel's id.
   */
  public String getChannelId() {
    return channelId;
  }

  /**
   * The file's id, for Channel file link.
   */
  public String getFileId() {
    return fileId;
  }

  /**
   * What the file holds. Other values can appear.
   * <ul>
   *   <li>voice: one party.
   *   <li>moh: the music on hold.
   * </ul>
   */
  public String getType() {
    return type;
  }

  /**
   * For a channel of type voice:
   * <ul>
   *   <li>CALLER: the caller's side.
   *   <li>CALLEE: the called side.
   * </ul>
   * <p>"" for any other channel.
   * <p>Side.UNRECOGNIZED for a value this library doesn't know; getSideValue() gives it.
   */
  public Side getSide() {
    return Side.fromValue(side);
  }

  /** The side as the API sent it. */
  public String getSideValue() {
    return side;
  }

  /**
   * For a channel of type voice, the step type the party came through (see timeline); "" for any other channel.
   */
  public String getVia() {
    return via;
  }

  /**
   * What the channel's step acted on, as in the timeline:
   * <ul>
   *   <li>FROM_SIP_PHONE: the phone that called, type PHONE.
   *   <li>RING_PHONE: the phone that rang, type PHONE.
   *   <li>FROM_SIP_PSTN: your phone number that was called, type PHONE_NUMBER.
   *   <li>Any other channel: null.
   * </ul>
   */
  public TimelineResource getResource() {
    return resource;
  }

  /**
   * The caller ID of the party on this channel.
   */
  public SessionCallerId getCallerId() {
    return callerId;
  }

  /**
   * The org's phone on this channel; null for an outside party or a channel without a party.
   */
  public RecordingChannelPhone getPhone() {
    return phone;
  }

  /**
   * The user that phone had during the call; null when it had none, or when there is no phone.
   */
  public RecordingChannelUser getUser() {
    return user;
  }

  /**
   * The extension the call reached that phone through: the closest one above its step in the timeline.
   * <ul>
   *   <li>FROM_SIP_PHONE: always null; a calling phone reaches no extension.
   *   <li>No extension above the step: null.
   *   <li>No phone on the channel: null.
   * </ul>
   */
  public ExtensionView getExtension() {
    return extension;
  }
}
