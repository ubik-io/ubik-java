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
 * What happens to calls.
 */
public final class SnapshotExtensionCalls {

  @SerializedName("phone_list_config")
  private Map<String, Object> phoneListConfig;
  @SerializedName("transfer_config")
  private Map<String, Object> transferConfig;
  @SerializedName("voicemail_config")
  private Map<String, Object> voicemailConfig;

  private SnapshotExtensionCalls() {}

  /**
   * The phones that ring.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getPhoneListConfig() {
    return phoneListConfig == null ? Collections.emptyMap() : Collections.unmodifiableMap(phoneListConfig);
  }

  /**
   * Where calls go after the phones ring.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getTransferConfig() {
    return transferConfig == null ? Collections.emptyMap() : Collections.unmodifiableMap(transferConfig);
  }

  /**
   * The voicemail, when nobody answers.
   * <p>As JSON values: String, Long or Double, Boolean, List, Map. Empty when absent.
   */
  public Map<String, Object> getVoicemailConfig() {
    return voicemailConfig == null ? Collections.emptyMap() : Collections.unmodifiableMap(voicemailConfig);
  }
}
