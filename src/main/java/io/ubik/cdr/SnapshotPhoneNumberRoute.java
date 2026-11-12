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
 * Where calls go.
 */
public final class SnapshotPhoneNumberRoute {

  @SerializedName("destination")
  private String destination;
  @SerializedName("destination_type")
  private String destinationType;

  private SnapshotPhoneNumberRoute() {}

  /**
   * The id of the destination; "" for HANGUP.
   */
  public String getDestination() {
    return destination;
  }

  /**
   * <ul>
   *   <li>EXTENSION: an extension.
   *   <li>PHONE_NUMBER: another of your phone numbers.
   *   <li>IVR: an automated attendant.
   *   <li>VOICEMAIL: a voicemail.
   *   <li>HANGUP: the call is hung up.
   * </ul>
   */
  public String getDestinationType() {
    return destinationType;
  }
}
