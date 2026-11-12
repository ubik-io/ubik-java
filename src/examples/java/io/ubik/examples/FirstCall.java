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

import io.ubik.Page;
import io.ubik.UBIK;
import io.ubik.cdr.CdrItem;
import io.ubik.cdr.ListCdrsRequest;

/** A first call: print the ids of the 10 latest CDRs. */
public final class FirstCall {

  private FirstCall() {}

  public static void run(UBIK client) {
    // [START first-call]
    Page<CdrItem> page = client.cdrs().list(
        ListCdrsRequest.newBuilder().setNbPerPage(10).build());
    for (CdrItem cdr : page.getData()) {
      System.out.println(cdr.getId());
    }
    // [END first-call]
  }
}
