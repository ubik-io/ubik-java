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

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import io.ubik.Page;
import io.ubik.UBIKException;
import io.ubik.internal.Json;
import io.ubik.internal.PageImpl;
import io.ubik.internal.Transport;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

/**
 * The CDRs of your organization: one record per call. Get it with
 * {@code client.cdrs()}.
 *
 * <p>The API key needs the right "Call history: Read".
 */
public final class Cdrs {

  private final Transport transport;

  /** Internal: get this service with {@code client.cdrs()}. */
  public Cdrs(Transport transport) {
    this.transport = transport;
  }

  /** The scope every CDR request needs. */
  static String scope(String orgId) {
    return "login:" + orgId + ":rw cdr:" + orgId + ":r";
  }

  /**
   * One CDR, with the call's steps, the resources it involved and its
   * recordings.
   *
   * @param id the CDR's id, for example from a cdr-add webhook
   * @throws io.ubik.NotFoundException when the CDR doesn't exist in this organization
   * @throws io.ubik.PermissionDeniedException when the API key lacks "Call history: Read"
   * @throws UBIKException for any other refusal or a network error
   */
  public CdrDetail get(String id) {
    Objects.requireNonNull(id, "id");
    HttpResponse<String> response =
        transport.get("/api/v1/cdr/" + Transport.encode(id), scope(transport.getOrgId()));
    Transport.check(response);
    return parse(response, CdrDetail.class);
  }

  /**
   * The first page of the CDRs the request selects, newest first unless the
   * request says otherwise. Use {@link Page#iterateAll()} for every page.
   *
   * @throws io.ubik.PermissionDeniedException when the API key lacks "Call history: Read"
   * @throws UBIKException for any other refusal (400 names the cause) or a network error
   */
  public Page<CdrItem> list(ListCdrsRequest request) {
    Objects.requireNonNull(request, "request");
    return listPage(request, 1);
  }

  private Page<CdrItem> listPage(ListCdrsRequest request, int page) {
    HttpResponse<String> response =
        transport.post(
            "/api/v1/cdr/list",
            request.toJson(transport.getOrgId(), page),
            scope(transport.getOrgId()));
    Transport.check(response);
    ListCdrsResponse body = parse(response, ListCdrsResponse.class);
    return new PageImpl<>(
        body.data, body.total, page, request.getNbPerPage(), next -> listPage(request, next));
  }

  static <T> T parse(HttpResponse<String> response, Class<T> type) {
    try {
      T value = Json.GSON.fromJson(response.body(), type);
      if (value == null) {
        throw new UBIKException("The response is empty", response.statusCode(), null);
      }
      return value;
    } catch (JsonParseException e) {
      throw new UBIKException("The response isn't valid JSON", response.statusCode(), e);
    }
  }

  private static final class ListCdrsResponse {
    @SerializedName("data")
    private List<CdrItem> data;

    @SerializedName("total")
    private long total;
  }
}
