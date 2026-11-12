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

package io.ubik.webhook;

import com.google.gson.JsonParseException;
import io.ubik.internal.Json;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Checks that a request comes from UBIK, as Standard Webhooks defines it,
 * then reads its event.
 *
 * <pre>{@code
 * WebhookVerifier verifier = WebhookVerifier.fromSecret(System.getenv("UBIK_WEBHOOK_SECRET"));
 *
 * // In your handler, with the body exactly as received:
 * WebhookEvent event = verifier.verify(
 *     request.getHeader("webhook-id"),
 *     request.getHeader("webhook-timestamp"),
 *     request.getHeader("webhook-signature"),
 *     body);
 * if (event.getType() == WebhookEventType.CDR_ADD) {
 *   CdrDetail cdr = client.cdrs().get(event.getCdrAddEvent().getCdrId());
 * }
 * }</pre>
 *
 * <p>Safe to share between threads.
 */
public final class WebhookVerifier {

  /** The farthest webhook-timestamp may be from this machine's clock. */
  public static final Duration TOLERANCE = Duration.ofMinutes(5);

  private static final String SECRET_PREFIX = "whsec_";
  private static final String SIGNATURE_PREFIX = "v1,";

  private final byte[] key;
  private final Clock clock;

  WebhookVerifier(byte[] key, Clock clock) {
    this.key = key.clone();
    this.clock = clock;
  }

  /**
   * A verifier for your webhook's signing secret.
   *
   * @param secret the signing secret, as the webhook's page shows it (whsec_ then base64)
   * @throws IllegalArgumentException when the secret isn't whsec_ then base64
   */
  public static WebhookVerifier fromSecret(String secret) {
    Objects.requireNonNull(secret, "secret");
    String encoded = secret.startsWith(SECRET_PREFIX) ? secret.substring(SECRET_PREFIX.length()) : secret;
    byte[] key;
    try {
      key = Base64.getDecoder().decode(encoded);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("The secret isn't whsec_ then base64");
    }
    if (key.length == 0) {
      throw new IllegalArgumentException("The secret is empty");
    }
    return new WebhookVerifier(key, Clock.systemUTC());
  }

  /**
   * Checks the request, then reads its event.
   *
   * <p>Pass the body exactly as received: a body parsed then written again
   * doesn't match its signature.
   *
   * @param webhookId the webhook-id header
   * @param webhookTimestamp the webhook-timestamp header
   * @param webhookSignature the webhook-signature header: one signature, or
   *     two separated by a space while you change the secret
   * @param body the request body, as received
   * @throws WebhookVerificationException when a header is missing, the time
   *     is more than {@link #TOLERANCE} from this machine's clock, no
   *     signature matches, or the body isn't an event
   */
  public WebhookEvent verify(
      String webhookId, String webhookTimestamp, String webhookSignature, String body) {
    if (webhookId == null || webhookId.isEmpty()) {
      throw new WebhookVerificationException("webhook-id is missing");
    }
    if (webhookTimestamp == null || !webhookTimestamp.matches("[0-9]{1,18}")) {
      throw new WebhookVerificationException("webhook-timestamp isn't Unix seconds");
    }
    if (webhookSignature == null || webhookSignature.isEmpty()) {
      throw new WebhookVerificationException("webhook-signature is missing");
    }
    if (body == null) {
      throw new WebhookVerificationException("The body is missing");
    }

    long sentAt = Long.parseLong(webhookTimestamp);
    long now = clock.instant().getEpochSecond();
    if (Math.abs(now - sentAt) > TOLERANCE.getSeconds()) {
      throw new WebhookVerificationException(
          "webhook-timestamp is more than " + TOLERANCE.toMinutes() + " minutes from this clock");
    }

    byte[] expected = sign(webhookId + "." + webhookTimestamp + "." + body);
    boolean matches = false;
    for (String signature : webhookSignature.split(" ")) {
      if (!signature.startsWith(SIGNATURE_PREFIX)) {
        continue;
      }
      byte[] given;
      try {
        given = Base64.getDecoder().decode(signature.substring(SIGNATURE_PREFIX.length()));
      } catch (IllegalArgumentException e) {
        continue;
      }
      // Constant time, so the comparison's duration tells nothing about the expected signature.
      matches |= MessageDigest.isEqual(expected, given);
    }
    if (!matches) {
      throw new WebhookVerificationException("No signature matches");
    }

    WebhookEvent event;
    try {
      event = Json.GSON.fromJson(body, WebhookEvent.class);
    } catch (JsonParseException e) {
      throw new WebhookVerificationException("The body isn't a JSON event");
    }
    if (event == null || event.getId() == null || event.getTypeValue() == null) {
      throw new WebhookVerificationException("The body isn't a JSON event");
    }
    return event;
  }

  private byte[] sign(String content) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("HMAC-SHA256 is missing from this Java", e);
    }
  }
}
