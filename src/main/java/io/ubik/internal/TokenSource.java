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

/** Gives access tokens per scope. Internal: not part of this library's API. */
public interface TokenSource {

  /** An access token holding this scope. */
  String getAccessToken(String scope);

  /** Forgets this scope's token, after the API refused it. */
  void invalidate(String scope);
}
