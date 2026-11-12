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
 * The phone number, when resource_type is PHONE_NUMBER.
 */
public final class PhoneNumberView {

  @SerializedName("id")
  private String id;
  @SerializedName("phone_number_e164")
  private String phoneNumberE164;

  private PhoneNumberView() {}

  /**
   * Its id.
   */
  public String getId() {
    return id;
  }

  /**
   * The number, in E.164 format (+15145550100).
   */
  public String getPhoneNumberE164() {
    return phoneNumberE164;
  }
}
