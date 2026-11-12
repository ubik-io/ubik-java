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
 * Snapshot of an IVR. Other fields can appear.
 */
public final class IvrSnapshot {

  @SerializedName("id")
  private String id;
  @SerializedName("org_id")
  private String orgId;
  @SerializedName("creation_timestamp_milli")
  private long creationTimestampMilli;
  @SerializedName("description")
  private String description;
  @SerializedName("language")
  private String language;
  @SerializedName("audio_file_id")
  private String audioFileId;
  @SerializedName("general_settings")
  private Map<String, Object> generalSettings;
  @SerializedName("incoming_calls")
  private Map<String, Object> incomingCalls;
  @SerializedName("after_audio_file_settings")
  private Map<String, Object> afterAudioFileSettings;

  private IvrSnapshot() {}

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
   * Language of its prompts: fr or en.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Its greeting's file id.
   */
  public String getAudioFileId() {
    return audioFileId;
  }

  /**
   * General settings.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getGeneralSettings() {
    return generalSettings == null ? Collections.emptyMap() : Collections.unmodifiableMap(generalSettings);
  }

  /**
   * Its menu: one option per key.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getIncomingCalls() {
    return incomingCalls == null ? Collections.emptyMap() : Collections.unmodifiableMap(incomingCalls);
  }

  /**
   * What happens when nobody presses a key.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getAfterAudioFileSettings() {
    return afterAudioFileSettings == null ? Collections.emptyMap() : Collections.unmodifiableMap(afterAudioFileSettings);
  }
}
