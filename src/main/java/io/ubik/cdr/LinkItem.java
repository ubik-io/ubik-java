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
 * A CDR linked to this one.
 */
public final class LinkItem {

  @SerializedName("linked_cdr_id")
  private String linkedCdrId;
  @SerializedName("kind")
  private String kind;
  @SerializedName("relation")
  private String relation;

  private LinkItem() {}

  /**
   * The linked CDR's id.
   */
  public String getLinkedCdrId() {
    return linkedCdrId;
  }

  /**
   * The kind.
   * <ul>
   *   <li>ATTENDED_TRANSFER: an attended transfer joined the two calls.
   * </ul>
   * <p>LinkKind.UNRECOGNIZED for a value this library doesn't know; getKindValue() gives it.
   */
  public LinkKind getKind() {
    return LinkKind.fromValue(kind);
  }

  /** The kind as the API sent it. */
  public String getKindValue() {
    return kind;
  }

  /**
   * The relation.
   * <ul>
   *   <li>CONSULT: the linked CDR is the consultation call's, which was joined to this call.
   *   <li>MERGED: this CDR is the consultation call's; the linked CDR is the call it joined.
   * </ul>
   * <p>LinkRelation.UNRECOGNIZED for a value this library doesn't know; getRelationValue() gives it.
   */
  public LinkRelation getRelation() {
    return LinkRelation.fromValue(relation);
  }

  /** The relation as the API sent it. */
  public String getRelationValue() {
    return relation;
  }
}
