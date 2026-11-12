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

import io.ubik.NotFoundException;
import io.ubik.PermissionDeniedException;
import io.ubik.UBIK;
import io.ubik.UBIKException;
import io.ubik.cdr.CdrDetail;

/** What each refusal looks like. */
public final class HandleErrors {

  private HandleErrors() {}

  /** @return the CDR, or null when it can't be read */
  public static CdrDetail getOrNull(UBIK client, String cdrId) {
    // [START errors]
    try {
      return client.cdrs().get(cdrId);
    } catch (NotFoundException e) {
      return null; // no such CDR in this organization
    } catch (PermissionDeniedException e) {
      throw e; // the API key lacks "Call history: Read"
    } catch (UBIKException e) {
      System.err.println("HTTP " + e.getStatusCode() + ": " + e.getMessage());
      return null; // 0 when there was no response
    }
    // [END errors]
  }
}
