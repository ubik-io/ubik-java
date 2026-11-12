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
import io.ubik.cdr.CdrDetail;

/** Get one CDR, for example the one a cdr-add webhook names. */
public final class GetCdr {

  private GetCdr() {}

  public static CdrDetail run(UBIK client, String cdrId) {
    // [START cdr-get]
    CdrDetail cdr = client.cdrs().get(cdrId);
    System.out.println(cdr.getDirection() + " " + cdr.getSrc() + " -> " + cdr.getDst());
    // [END cdr-get]
    return cdr;
  }
}
