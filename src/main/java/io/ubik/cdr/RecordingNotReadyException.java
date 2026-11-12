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

import io.ubik.UBIKException;

/**
 * HTTP 409: the recording's file isn't ready yet (state IN_PROGRESS or
 * RECORDING_COMPLETE). Try again later.
 */
public class RecordingNotReadyException extends UBIKException {

  private static final long serialVersionUID = 1L;

  private final String state;

  public RecordingNotReadyException(String message, String state) {
    super(message, 409, null);
    this.state = state;
  }

  /** The recording's state, as the API sent it. */
  public String getState() {
    return state;
  }
}
