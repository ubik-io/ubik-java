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

import io.ubik.cdr.Cdrs;
import io.ubik.cdr.Recordings;
import io.ubik.internal.TokenSource;
import io.ubik.internal.Transport;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;

/**
 * A UBIK API client. Create it with {@link UBIKOptions#getService()}.
 *
 * <p>The client gets its access tokens itself, with the options' API key,
 * and gets a new one before each expires. Safe to use from several threads.
 */
public final class UBIK {

  private final UBIKOptions options;
  private final AccessTokenRefresher accessTokenRefresher;
  private final Cdrs cdrs;
  private final Recordings recordings;

  UBIK(UBIKOptions options) {
    this.options = options;
    HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    this.accessTokenRefresher =
        new AccessTokenRefresher(
            options.getCredentials(), options.getHost(), httpClient, Clock.systemUTC());
    Transport transport =
        new Transport(
            httpClient,
            options.getHost(),
            options.getOrgId(),
            new TokenSource() {
              @Override
              public String getAccessToken(String scope) {
                return accessTokenRefresher.getAccessToken(scope);
              }

              @Override
              public void invalidate(String scope) {
                accessTokenRefresher.invalidate(scope);
              }
            },
            Version.USER_AGENT);
    this.cdrs = new Cdrs(transport);
    this.recordings = new Recordings(transport);
  }

  /** The CDRs of the organization: one record per call. */
  public Cdrs cdrs() {
    return cdrs;
  }

  /** The files of the CDRs' recordings. */
  public Recordings recordings() {
    return recordings;
  }

  /** The options this client was created with. */
  public UBIKOptions getOptions() {
    return options;
  }


}
