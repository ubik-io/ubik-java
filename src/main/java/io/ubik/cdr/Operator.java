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

/** How a filter's field compares to its values. */
public enum Operator {
  /** = */
  EQUAL("="),
  /** != */
  NOT_EQUAL("!="),
  /** &gt; */
  GREATER(">"),
  /** &gt;= */
  GREATER_OR_EQUAL(">="),
  /** &lt; */
  LESS("<"),
  /** &lt;= */
  LESS_OR_EQUAL("<=");

  private final String value;

  Operator(String value) {
    this.value = value;
  }

  /** The operator on the wire. */
  public String getValue() {
    return value;
  }
}
