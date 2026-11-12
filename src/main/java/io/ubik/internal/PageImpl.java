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

import io.ubik.Page;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;

/** A page that fetches the next ones by number. Internal: not part of this library's API. */
public final class PageImpl<T> implements Page<T> {

  private final List<T> data;
  private final long total;
  private final int page;
  private final int nbPerPage;
  private final IntFunction<Page<T>> fetch;

  /**
   * @param page this page's number, from 1
   * @param fetch requests a page by number
   */
  public PageImpl(List<T> data, long total, int page, int nbPerPage, IntFunction<Page<T>> fetch) {
    this.data = data == null ? Collections.emptyList() : Collections.unmodifiableList(data);
    this.total = total;
    this.page = page;
    this.nbPerPage = nbPerPage;
    this.fetch = fetch;
  }

  @Override
  public List<T> getData() {
    return data;
  }

  @Override
  public long getTotal() {
    return total;
  }

  @Override
  public boolean hasNextPage() {
    return !data.isEmpty() && (long) page * nbPerPage < total;
  }

  @Override
  public Page<T> getNextPage() {
    return hasNextPage() ? fetch.apply(page + 1) : null;
  }

  @Override
  public Iterable<T> iterateAll() {
    return () ->
        new Iterator<T>() {
          private Page<T> current = PageImpl.this;
          private Iterator<T> items = data.iterator();

          @Override
          public boolean hasNext() {
            while (!items.hasNext()) {
              if (current == null || !current.hasNextPage()) {
                return false;
              }
              current = current.getNextPage();
              items = current == null ? Collections.emptyIterator() : current.getData().iterator();
            }
            return true;
          }

          @Override
          public T next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            return items.next();
          }
        };
  }
}
