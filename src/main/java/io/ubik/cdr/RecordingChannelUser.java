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
 * A user of the org, as they are now (as they were during the call once deleted).
 */
public final class RecordingChannelUser {

  @SerializedName("id")
  private String id;
  @SerializedName("fullname")
  private String fullname;
  @SerializedName("auth_user_email")
  private String authUserEmail;

  private RecordingChannelUser() {}

  /**
   * The user's id.
   */
  public String getId() {
    return id;
  }

  /**
   * Their full name.
   */
  public String getFullname() {
    return fullname;
  }

  /**
   * The email they sign in with; "" when unknown.
   */
  public String getAuthUserEmail() {
    return authUserEmail;
  }
}
