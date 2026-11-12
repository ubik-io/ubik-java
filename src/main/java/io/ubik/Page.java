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

package io.ubik;

import java.util.List;

/**
 * One page of a list, and the way to the next pages.
 *
 * <pre>{@code
 * for (CdrItem cdr : client.cdrs().list(request).iterateAll()) {
 *   // every CDR of every page
 * }
 * }</pre>
 *
 * @param <T> the item type
 */
public interface Page<T> {

  /** The items of this page. */
  List<T> getData();

  /** How many items match, all pages together. */
  long getTotal();

  /** True when a page follows this one. */
  boolean hasNextPage();

  /** The next page, from a new request; null when this is the last one. */
  Page<T> getNextPage();

  /**
   * Every item from this page on. Each further page is requested as the loop
   * reaches it, so an item added during the loop can shift the pages, as with
   * a loop written by hand.
   */
  Iterable<T> iterateAll();
}
