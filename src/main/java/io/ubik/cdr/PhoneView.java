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
 * The phone, when resource_type is PHONE.
 */
public final class PhoneView {

  @SerializedName("id")
  private String id;
  @SerializedName("description")
  private String description;
  @SerializedName("user")
  private UserView user;

  private PhoneView() {}

  /**
   * The phone's id.
   */
  public String getId() {
    return id;
  }

  /**
   * Its name.
   */
  public String getDescription() {
    return description;
  }

  /**
   * The phone's user; null when it has none.
   */
  public UserView getUser() {
    return user;
  }
}
