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

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import io.ubik.internal.Json;

/**
 * The resource a step acted on, as it was during the call.
 */
public final class TimelineResource {

  @SerializedName("type")
  private String type;
  @SerializedName("id")
  private String id;
  @SerializedName("label")
  private String label;
  @SerializedName("snapshot")
  private JsonElement snapshot;

  private TimelineResource() {}

  /**
   * The resource type, such as PHONE, EXTENSION, IVR or PHONE_NUMBER.
   */
  public String getType() {
    return type;
  }

  /**
   * The resource's id.
   */
  public String getId() {
    return id;
  }

  /**
   * Its name during the call.
   */
  public String getLabel() {
    return label;
  }

  /** The PHONE resource as it was during the call: set when getType() is PHONE, null otherwise. */
  public PhoneSnapshot getPhoneSnapshot() {
    return "PHONE".equals(type) && snapshot != null && snapshot.isJsonObject() ? Json.GSON.fromJson(snapshot, PhoneSnapshot.class) : null;
  }

  /** The PHONE_NUMBER resource as it was during the call: set when getType() is PHONE_NUMBER, null otherwise. */
  public PhoneNumberSnapshot getPhoneNumberSnapshot() {
    return "PHONE_NUMBER".equals(type) && snapshot != null && snapshot.isJsonObject() ? Json.GSON.fromJson(snapshot, PhoneNumberSnapshot.class) : null;
  }

  /** The EXTENSION resource as it was during the call: set when getType() is EXTENSION, null otherwise. */
  public ExtensionSnapshot getExtensionSnapshot() {
    return "EXTENSION".equals(type) && snapshot != null && snapshot.isJsonObject() ? Json.GSON.fromJson(snapshot, ExtensionSnapshot.class) : null;
  }

  /** The IVR resource as it was during the call: set when getType() is IVR, null otherwise. */
  public IvrSnapshot getIvrSnapshot() {
    return "IVR".equals(type) && snapshot != null && snapshot.isJsonObject() ? Json.GSON.fromJson(snapshot, IvrSnapshot.class) : null;
  }
}
