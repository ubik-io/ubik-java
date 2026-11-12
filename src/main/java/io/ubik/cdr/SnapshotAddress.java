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
 * An address.
 */
public final class SnapshotAddress {

  @SerializedName("line1")
  private String line1;
  @SerializedName("line2")
  private String line2;
  @SerializedName("city")
  private String city;
  @SerializedName("zone_code")
  private String zoneCode;
  @SerializedName("postal_code")
  private String postalCode;
  @SerializedName("country_code")
  private String countryCode;

  private SnapshotAddress() {}

  /**
   * Street address.
   */
  public String getLine1() {
    return line1;
  }

  /**
   * Second address line; "" when there is none.
   */
  public String getLine2() {
    return line2;
  }

  /**
   * City.
   */
  public String getCity() {
    return city;
  }

  /**
   * Province or state code.
   */
  public String getZoneCode() {
    return zoneCode;
  }

  /**
   * Postal or ZIP code.
   */
  public String getPostalCode() {
    return postalCode;
  }

  /**
   * Country code, ISO 3166-1 alpha-2.
   */
  public String getCountryCode() {
    return countryCode;
  }
}
