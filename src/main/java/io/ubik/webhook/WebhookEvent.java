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

package io.ubik.webhook;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import io.ubik.internal.Json;

/**
 * An event UBIK sent to your webhook, after {@link WebhookVerifier#verify}
 * checked it.
 *
 * <p>Answer every event with a 2xx within 10 seconds, also the types you
 * don't handle: any other answer is attempted again, and after 9 failed
 * attempts the webhook becomes Inactive.
 */
public final class WebhookEvent {

  @SerializedName("id")
  private String id;

  @SerializedName("type")
  private String type;

  @SerializedName("org_id")
  private String orgId;

  @SerializedName("api_version")
  private String apiVersion;

  @SerializedName("creation_timestamp_milli")
  private long creationTimestampMilli;

  @SerializedName("data")
  private JsonElement data;

  private WebhookEvent() {}

  /** The event's id, as in webhook-id, the same on every attempt: use it to ignore a repeat. */
  public String getId() {
    return id;
  }

  /**
   * The event type. WebhookEventType.UNRECOGNIZED for a type this library
   * doesn't know; getTypeValue() gives it.
   */
  public WebhookEventType getType() {
    return WebhookEventType.fromValue(type);
  }

  /** The event type as the API sent it. */
  public String getTypeValue() {
    return type;
  }

  /** Your organization's id. */
  public String getOrgId() {
    return orgId;
  }

  /** The body's version. New fields may be added to data; none is removed. */
  public String getApiVersion() {
    return apiVersion;
  }

  /** When the event was made, in Unix milliseconds; the same on every attempt. */
  public long getCreationTimestampMilli() {
    return creationTimestampMilli;
  }

  /** What a cdr-add event is about: set when getType() is CDR_ADD, null otherwise. */
  public CdrAddEvent getCdrAddEvent() {
    return WebhookEventType.CDR_ADD.getValue().equals(type) && data != null && data.isJsonObject()
        ? Json.GSON.fromJson(data, CdrAddEvent.class)
        : null;
  }
}
