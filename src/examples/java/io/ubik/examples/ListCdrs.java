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

package io.ubik.examples;

import io.ubik.UBIK;
import io.ubik.cdr.CdrItem;
import io.ubik.cdr.ListCdrsRequest;
import io.ubik.cdr.Operator;
import java.util.ArrayList;
import java.util.List;

/** List CDRs over a period. */
public final class ListCdrs {

  private ListCdrs() {}

  /**
   * Every CDR whose call started in [from, to), oldest first.
   *
   * @param from Unix milliseconds
   * @param to Unix milliseconds
   */
  public static List<CdrItem> window(UBIK client, long from, long to) {
    List<CdrItem> cdrs = new ArrayList<>();
    // [START cdr-list-window]
    ListCdrsRequest request = ListCdrsRequest.newBuilder()
        .setStartTimestampMilli(Operator.GREATER_OR_EQUAL, from)
        .setStartTimestampMilli(Operator.LESS, to)
        .setIsAscending(true)
        .build();
    for (CdrItem cdr : client.cdrs().list(request).iterateAll()) {
      cdrs.add(cdr);
    }
    // [END cdr-list-window]
    return cdrs;
  }

  /** The CDRs of the default period that have at least one recording. */
  public static List<CdrItem> recorded(UBIK client) {
    List<CdrItem> recorded = new ArrayList<>();
    // [START cdr-list-recorded]
    ListCdrsRequest request = ListCdrsRequest.newBuilder().build();
    for (CdrItem cdr : client.cdrs().list(request).iterateAll()) {
      if (cdr.getRecordingSessionCount() > 0) {
        recorded.add(cdr);
      }
    }
    // [END cdr-list-recorded]
    return recorded;
  }
}
