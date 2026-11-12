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
import java.util.List;

/**
 * A resource the call involved, as it is now.
 */
public final class ResourceRef {

  @SerializedName("resource_type")
  private String resourceType;
  @SerializedName("resource_id")
  private String resourceId;
  @SerializedName("label_snapshot")
  private String labelSnapshot;
  @SerializedName("deleted")
  private boolean deleted;
  @SerializedName("roles")
  private List<String> roles;
  @SerializedName("extension")
  private ExtensionView extension;
  @SerializedName("phone")
  private PhoneView phone;
  @SerializedName("user")
  private UserView user;
  @SerializedName("ivr")
  private IvrView ivr;
  @SerializedName("phone_number")
  private PhoneNumberView phoneNumber;

  private ResourceRef() {}

  /**
   * The resource type.
   * <ul>
   *   <li>EXTENSION: an extension.
   *   <li>PHONE: a phone.
   *   <li>USER: a user.
   *   <li>IVR: an automated attendant.
   *   <li>PHONE_NUMBER: one of your organization's phone numbers.
   *   <li>NUMBER: an outside number.
   * </ul>
   * <p>ResourceType.UNRECOGNIZED for a value this library doesn't know; getResourceTypeValue() gives it.
   */
  public ResourceType getResourceType() {
    return ResourceType.fromValue(resourceType);
  }

  /** The resource_type as the API sent it. */
  public String getResourceTypeValue() {
    return resourceType;
  }

  /**
   * The resource's id; for a NUMBER, the number itself.
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * Its name during the call.
   */
  public String getLabelSnapshot() {
    return labelSnapshot;
  }

  /**
   * True when the resource no longer exists.
   */
  public boolean getDeleted() {
    return deleted;
  }

  /**
   * What it was in the call. Other values can appear.
   * <ul>
   *   <li>CALLER: it placed the call.
   *   <li>CALLED: it was called.
   *   <li>CALLER_ID: the number shown to the person called.
   *   <li>A step type (see timeline): it took part in that step.
   * </ul>
   * <p>Empty when absent.
   */
  public List<String> getRoles() {
    return roles == null ? Collections.emptyList() : Collections.unmodifiableList(roles);
  }

  /**
   * The extension.
   * <p>Null when absent.
   */
  public ExtensionView getExtension() {
    return extension;
  }

  /**
   * The phone.
   * <p>Null when absent.
   */
  public PhoneView getPhone() {
    return phone;
  }

  /**
   * The user.
   * <p>Null when absent.
   */
  public UserView getUser() {
    return user;
  }

  /**
   * The ivr.
   * <p>Null when absent.
   */
  public IvrView getIvr() {
    return ivr;
  }

  /**
   * The phone_number.
   * <p>Null when absent.
   */
  public PhoneNumberView getPhoneNumber() {
    return phoneNumber;
  }
}
