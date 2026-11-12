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

package io.ubik.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

/** The JSON settings of this library. Internal: not part of its API. */
public final class Json {

  /** Reads free-form numbers as Long when whole, Double otherwise. */
  public static final Gson GSON =
      new GsonBuilder()
          .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
          .disableHtmlEscaping()
          .create();

  private Json() {}
}
