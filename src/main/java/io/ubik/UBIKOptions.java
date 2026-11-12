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

import java.util.Objects;

/**
 * The settings of a UBIK client.
 *
 * <pre>{@code
 * UBIK client = UBIKOptions.newBuilder()
 *     .setOrgId("7d1f3a5c-2b4e-4f6a-8c9d-0e1f2a3b4c5d")
 *     .setCredentials(credentials)
 *     .build()
 *     .getService();
 * }</pre>
 */
public final class UBIKOptions {

  /** The UBIK API's address. */
  public static final String DEFAULT_HOST = "https://ubik.io";

  private final String orgId;
  private final ApiKeyCredentials credentials;
  private final String host;

  private UBIKOptions(Builder builder) {
    this.orgId = builder.orgId;
    this.credentials = builder.credentials;
    this.host = builder.host;
  }

  /** Starts new options. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** The organization the client works in. */
  public String getOrgId() {
    return orgId;
  }

  /** The API key the client signs in with. */
  public ApiKeyCredentials getCredentials() {
    return credentials;
  }

  /** The UBIK API's address, without a trailing slash. */
  public String getHost() {
    return host;
  }

  /**
   * Creates a client with these options. Create it once and share it: it
   * keeps its access tokens and is safe to use from several threads.
   */
  public UBIK getService() {
    return new UBIK(this);
  }

  /** Builds {@link UBIKOptions}. setOrgId and setCredentials are required. */
  public static final class Builder {
    private String orgId;
    private ApiKeyCredentials credentials;
    private String host = DEFAULT_HOST;

    private Builder() {}

    /** Required. The organization the client works in: the API key's organization. */
    public Builder setOrgId(String orgId) {
      this.orgId = Objects.requireNonNull(orgId, "orgId");
      return this;
    }

    /** Required. The API key the client signs in with. */
    public Builder setCredentials(ApiKeyCredentials credentials) {
      this.credentials = Objects.requireNonNull(credentials, "credentials");
      return this;
    }

    /** The UBIK API's address. Default: {@value UBIKOptions#DEFAULT_HOST}. */
    public Builder setHost(String host) {
      Objects.requireNonNull(host, "host");
      while (host.endsWith("/")) {
        host = host.substring(0, host.length() - 1);
      }
      if (!host.startsWith("https://") && !host.startsWith("http://")) {
        throw new IllegalArgumentException("host must start with https://");
      }
      this.host = host;
      return this;
    }

    /**
     * Builds the options.
     *
     * @throws IllegalStateException when setOrgId or setCredentials wasn't called
     */
    public UBIKOptions build() {
      if (orgId == null || orgId.isEmpty()) {
        throw new IllegalStateException("setOrgId is required");
      }
      if (credentials == null) {
        throw new IllegalStateException("setCredentials is required");
      }
      return new UBIKOptions(this);
    }
  }
}
