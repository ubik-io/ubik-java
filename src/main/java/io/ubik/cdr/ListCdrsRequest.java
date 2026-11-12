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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Which CDRs to list, in which order, and how many per page.
 *
 * <pre>{@code
 * ListCdrsRequest request = ListCdrsRequest.newBuilder()
 *     .setStartTimestampMilli(Operator.GREATER_OR_EQUAL, from)
 *     .setStartTimestampMilli(Operator.LESS, to)
 *     .addDirection(Direction.INBOUND_PSTN)
 *     .build();
 * }</pre>
 *
 * <p>Without a lower bound on start_timestamp_milli, the API lists the 31 days
 * before the upper bound, or before now when there is none. The period may
 * span at most 93 days.
 */
public final class ListCdrsRequest {

  /** The most CDRs per page the API gives. */
  public static final int MAX_NB_PER_PAGE = 100;

  private final ResourceType resourceType;
  private final String resourceId;
  private final List<Operator> startTimestampMilliOperators;
  private final List<Long> startTimestampMilliValues;
  private final List<Direction> directions;
  private final boolean isAscending;
  private final int nbPerPage;

  private ListCdrsRequest(Builder builder) {
    this.resourceType = builder.resourceType;
    this.resourceId = builder.resourceId;
    this.startTimestampMilliOperators = new ArrayList<>(builder.startTimestampMilliOperators);
    this.startTimestampMilliValues = new ArrayList<>(builder.startTimestampMilliValues);
    this.directions = new ArrayList<>(builder.directions);
    this.isAscending = builder.isAscending;
    this.nbPerPage = builder.nbPerPage;
  }

  /** Starts a new request: every CDR of the default period, newest first. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** The resource type, or null for every CDR. */
  public ResourceType getResourceType() {
    return resourceType;
  }

  /** The resource's id, or null. */
  public String getResourceId() {
    return resourceId;
  }

  /** The call directions kept; empty for all. */
  public List<Direction> getDirections() {
    return Collections.unmodifiableList(directions);
  }

  /** Oldest first when true; newest first otherwise. */
  public boolean getIsAscending() {
    return isAscending;
  }

  /** CDRs per page. */
  public int getNbPerPage() {
    return nbPerPage;
  }

  /** The request body for one page. Internal: not part of this library's API. */
  public String toJson(String orgId, int page) {
    JsonArray operationList = new JsonArray();
    if (resourceId != null) {
      operationList.add(operation("resource_id", Operator.EQUAL, Collections.singletonList(resourceId)));
    }
    for (int i = 0; i < startTimestampMilliOperators.size(); i++) {
      operationList.add(
          operation(
              "start_timestamp_milli",
              startTimestampMilliOperators.get(i),
              Collections.singletonList(Long.toString(startTimestampMilliValues.get(i)))));
    }
    if (!directions.isEmpty()) {
      List<String> values = new ArrayList<>();
      for (Direction direction : directions) {
        values.add(direction.getValue());
      }
      operationList.add(operation("direction", Operator.EQUAL, values));
    }

    JsonObject filter = new JsonObject();
    filter.add("operation_list", operationList);
    JsonObject sort = new JsonObject();
    sort.addProperty("field_name", "start_timestamp_milli");
    sort.addProperty("is_ascending", isAscending);
    JsonObject limit = new JsonObject();
    limit.addProperty("nb_per_page", nbPerPage);
    limit.addProperty("page", page);
    JsonObject listQuery = new JsonObject();
    listQuery.add("filter", filter);
    listQuery.add("sort", sort);
    listQuery.add("limit", limit);

    JsonObject body = new JsonObject();
    body.addProperty("org_id", orgId);
    if (resourceType != null) {
      body.addProperty("resource_type", resourceType.getValue());
    }
    body.add("list_query", listQuery);
    return body.toString();
  }

  private static JsonObject operation(String fieldName, Operator operator, List<String> values) {
    JsonObject operation = new JsonObject();
    operation.addProperty("field_name", fieldName);
    operation.addProperty("operator", operator.getValue());
    JsonArray array = new JsonArray();
    values.forEach(array::add);
    operation.add("values", array);
    return operation;
  }

  /** Builds a {@link ListCdrsRequest}. */
  public static final class Builder {
    private ResourceType resourceType;
    private String resourceId;
    private final List<Operator> startTimestampMilliOperators = new ArrayList<>();
    private final List<Long> startTimestampMilliValues = new ArrayList<>();
    private final Set<Direction> directions = new LinkedHashSet<>();
    private boolean isAscending;
    private int nbPerPage = MAX_NB_PER_PAGE;

    private Builder() {}

    /** Only the CDRs that touched a resource of this type. */
    public Builder setResourceType(ResourceType resourceType) {
      Objects.requireNonNull(resourceType, "resourceType");
      if (resourceType == ResourceType.UNRECOGNIZED) {
        throw new IllegalArgumentException("resourceType can't be UNRECOGNIZED");
      }
      this.resourceType = resourceType;
      return this;
    }

    /** Only the CDRs of this resource, of the resource type set; for NUMBER, the number. */
    public Builder setResourceId(String resourceId) {
      this.resourceId = Objects.requireNonNull(resourceId, "resourceId");
      return this;
    }

    /**
     * Keeps the calls whose start compares to this time, in Unix
     * milliseconds. Call it twice for a period.
     */
    public Builder setStartTimestampMilli(Operator operator, long startTimestampMilli) {
      startTimestampMilliOperators.add(Objects.requireNonNull(operator, "operator"));
      startTimestampMilliValues.add(startTimestampMilli);
      return this;
    }

    /** Keeps the calls of this direction; call it again to keep several. */
    public Builder addDirection(Direction direction) {
      Objects.requireNonNull(direction, "direction");
      if (direction == Direction.UNRECOGNIZED) {
        throw new IllegalArgumentException("direction can't be UNRECOGNIZED");
      }
      directions.add(direction);
      return this;
    }

    /** Oldest first when true; newest first otherwise (the default). */
    public Builder setIsAscending(boolean isAscending) {
      this.isAscending = isAscending;
      return this;
    }

    /** CDRs per page, from 1 to {@value ListCdrsRequest#MAX_NB_PER_PAGE} (the default). */
    public Builder setNbPerPage(int nbPerPage) {
      if (nbPerPage < 1 || nbPerPage > MAX_NB_PER_PAGE) {
        throw new IllegalArgumentException("nbPerPage must be between 1 and " + MAX_NB_PER_PAGE);
      }
      this.nbPerPage = nbPerPage;
      return this;
    }

    /**
     * Builds the request.
     *
     * @throws IllegalStateException when a resource id is set without a resource type
     */
    public ListCdrsRequest build() {
      if (resourceId != null && resourceType == null) {
        throw new IllegalStateException("setResourceId needs setResourceType");
      }
      return new ListCdrsRequest(this);
    }
  }
}
