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
 * A recorded phone.
 */
public final class RecordingPhone {

  @SerializedName("phone_id")
  private String phoneId;
  @SerializedName("user_id")
  private String userId;

  private RecordingPhone() {}

  /**
   * The phone's id.
   */
  public String getPhoneId() {
    return phoneId;
  }

  /**
   * Its user's id.
   */
  public String getUserId() {
    return userId;
  }
}
