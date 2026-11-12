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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ubik.cdr.Direction;
import io.ubik.cdr.Disposition;
import io.ubik.cdr.ListCdrsRequest;
import io.ubik.cdr.ResourceType;
import io.ubik.cdr.Side;
import org.junit.jupiter.api.Test;

/**
 * The request builder refuses what the API would refuse with a 400, and
 * enums read a value this library doesn't know as UNRECOGNIZED instead of
 * failing (decision PR3-1: a new API value never breaks a running client).
 */
class ListCdrsRequestTest {

  @Test
  void defaultsAreEveryCdrNewestFirstAHundredPerPage() {
    JsonObject body =
        JsonParser.parseString(ListCdrsRequest.newBuilder().build().toJson(TestKeys.ORG_ID, 1))
            .getAsJsonObject();
    assertEquals(false, body.has("resource_type"));
    JsonObject query = body.getAsJsonObject("list_query");
    assertEquals(0, query.getAsJsonObject("filter").getAsJsonArray("operation_list").size());
    assertEquals(false, query.getAsJsonObject("sort").get("is_ascending").getAsBoolean());
    assertEquals(100, query.getAsJsonObject("limit").get("nb_per_page").getAsInt());
  }

  @Test
  void nbPerPageStaysBetweenOneAndAHundred() {
    assertThrows(IllegalArgumentException.class, () -> ListCdrsRequest.newBuilder().setNbPerPage(0));
    assertThrows(IllegalArgumentException.class, () -> ListCdrsRequest.newBuilder().setNbPerPage(101));
    assertEquals(1, ListCdrsRequest.newBuilder().setNbPerPage(1).build().getNbPerPage());
    assertEquals(100, ListCdrsRequest.newBuilder().setNbPerPage(100).build().getNbPerPage());
  }

  @Test
  void aResourceIdNeedsAResourceType() {
    IllegalStateException e =
        assertThrows(
            IllegalStateException.class,
            () -> ListCdrsRequest.newBuilder().setResourceId("ext-1").build());
    assertEquals("setResourceId needs setResourceType", e.getMessage());
  }

  @Test
  void unrecognizedIsNotAFilter() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ListCdrsRequest.newBuilder().addDirection(Direction.UNRECOGNIZED));
    assertThrows(
        IllegalArgumentException.class,
        () -> ListCdrsRequest.newBuilder().setResourceType(ResourceType.UNRECOGNIZED));
  }

  @Test
  void aNewApiValueReadsAsUnrecognized() {
    assertEquals(Disposition.UNRECOGNIZED, Disposition.fromValue("SOMETHING_NEW"));
    assertEquals(Disposition.NO_ANSWER, Disposition.fromValue("NO_ANSWER"));
    assertNull(Disposition.fromValue(null));
    assertNull(Disposition.UNRECOGNIZED.getValue());
    assertEquals(Side.UNSPECIFIED, Side.fromValue(""));
  }
}
