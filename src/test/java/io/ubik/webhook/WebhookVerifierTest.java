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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.models.media.Schema;
import io.ubik.PublicOpenAPI;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.TreeSet;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

/**
 * The verifier accepts the signed test vector of the public webhook file and
 * refuses every request it should: a changed byte, another secret, an old or
 * future time, a malformed signature. A verifier that accepted one of them
 * would let anyone make a receiver act on a forged or replayed call.
 */
class WebhookVerifierTest {

  @SuppressWarnings("unchecked")
  private static final Map<String, Object> VECTOR =
      (Map<String, Object>)
          ((Map<String, Object>)
                  ((Map<String, Object>)
                          ((Map<String, Object>)
                                  PublicOpenAPI.WEBHOOK.model().getExtensions().get("x-webhooks"))
                              .get("cdr-add"))
                      .get("post"))
              .get("x-signature-test-vector");

  private static final String SECRET = (String) VECTOR.get("secret");
  private static final String ID = (String) VECTOR.get("webhook-id");
  private static final String TIMESTAMP = (String) VECTOR.get("webhook-timestamp");
  private static final String BODY = (String) VECTOR.get("body");
  private static final String SIGNATURE = (String) VECTOR.get("webhook-signature");

  private static WebhookVerifier at(long epochSecond) {
    byte[] key = Base64.getDecoder().decode(SECRET.substring("whsec_".length()));
    return new WebhookVerifier(key, Clock.fixed(Instant.ofEpochSecond(epochSecond), ZoneOffset.UTC));
  }

  private static final WebhookVerifier VERIFIER = at(Long.parseLong(TIMESTAMP));

  @Test
  void theTestVectorVerifiesAndReads() {
    WebhookEvent event = VERIFIER.verify(ID, TIMESTAMP, SIGNATURE, BODY);
    assertEquals(ID, event.getId());
    assertEquals(WebhookEventType.CDR_ADD, event.getType());
    assertEquals("cdr-add", event.getTypeValue());
    assertEquals("7d1f3a5c-2b4e-4f6a-8c9d-0e1f2a3b4c5d", event.getOrgId());
    assertEquals("1.0", event.getApiVersion());
    assertEquals(1794268800000L, event.getCreationTimestampMilli());
    assertEquals("8f1c2d3e-4a5b-4c6d-9e7f-0a1b2c3d4e5f", event.getCdrAddEvent().getCdrId());
  }

  @Test
  void fromSecretReadsTheSecretAsThePageShowsIt() {
    // The real clock is far from the vector's time: only the time check may refuse it.
    WebhookVerificationException e =
        assertThrows(
            WebhookVerificationException.class,
            () -> WebhookVerifier.fromSecret(SECRET).verify(ID, TIMESTAMP, SIGNATURE, BODY));
    assertEquals("webhook-timestamp is more than 5 minutes from this clock", e.getMessage());
    assertThrows(IllegalArgumentException.class, () -> WebhookVerifier.fromSecret("whsec_!!!"));
    assertThrows(IllegalArgumentException.class, () -> WebhookVerifier.fromSecret("whsec_"));
  }

  @Test
  void eachChangedByteOfTheBodyIsRefused() {
    for (int i = 0; i < BODY.length(); i++) {
      char c = BODY.charAt(i);
      String changed = BODY.substring(0, i) + (char) (c == 'a' ? 'b' : 'a') + BODY.substring(i + 1);
      final String body = changed;
      assertThrows(
          WebhookVerificationException.class,
          () -> VERIFIER.verify(ID, TIMESTAMP, SIGNATURE, body),
          "byte " + i);
    }
  }

  @Test
  void anotherIdOrTimestampIsRefused() {
    assertThrows(
        WebhookVerificationException.class,
        () -> VERIFIER.verify(ID + "x", TIMESTAMP, SIGNATURE, BODY));
    String next = Long.toString(Long.parseLong(TIMESTAMP) + 1);
    assertThrows(
        WebhookVerificationException.class, () -> VERIFIER.verify(ID, next, SIGNATURE, BODY));
  }

  @Test
  void anotherSecretIsRefused() {
    WebhookVerifier other =
        new WebhookVerifier(
            "another-secret".getBytes(StandardCharsets.US_ASCII),
            Clock.fixed(Instant.ofEpochSecond(Long.parseLong(TIMESTAMP)), ZoneOffset.UTC));
    WebhookVerificationException e =
        assertThrows(
            WebhookVerificationException.class, () -> other.verify(ID, TIMESTAMP, SIGNATURE, BODY));
    assertEquals("No signature matches", e.getMessage());
  }

  @Test
  void theTimeWindowIsFiveMinutesEachWay() {
    long sent = Long.parseLong(TIMESTAMP);
    at(sent + 300).verify(ID, TIMESTAMP, SIGNATURE, BODY);
    at(sent - 300).verify(ID, TIMESTAMP, SIGNATURE, BODY);
    assertThrows(
        WebhookVerificationException.class,
        () -> at(sent + 301).verify(ID, TIMESTAMP, SIGNATURE, BODY));
    assertThrows(
        WebhookVerificationException.class,
        () -> at(sent - 301).verify(ID, TIMESTAMP, SIGNATURE, BODY));
  }

  @Test
  void duringASecretChangeOneMatchingSignatureIsEnough() {
    String old = "v1," + Base64.getEncoder().encodeToString(new byte[32]);
    VERIFIER.verify(ID, TIMESTAMP, old + " " + SIGNATURE, BODY);
    VERIFIER.verify(ID, TIMESTAMP, SIGNATURE + " " + old, BODY);
    assertThrows(
        WebhookVerificationException.class, () -> VERIFIER.verify(ID, TIMESTAMP, old + " " + old, BODY));
  }

  @Test
  void aMalformedSignatureIsRefused() {
    String digest = SIGNATURE.substring("v1,".length());
    for (String signature : new String[] {digest, "v2," + digest, "v1,", "v1,!!!", " "}) {
      assertThrows(
          WebhookVerificationException.class,
          () -> VERIFIER.verify(ID, TIMESTAMP, signature, BODY),
          signature);
    }
  }

  @Test
  void aMissingOrMalformedHeaderIsRefused() {
    assertThrows(WebhookVerificationException.class, () -> VERIFIER.verify(null, TIMESTAMP, SIGNATURE, BODY));
    assertThrows(WebhookVerificationException.class, () -> VERIFIER.verify("", TIMESTAMP, SIGNATURE, BODY));
    assertThrows(WebhookVerificationException.class, () -> VERIFIER.verify(ID, null, SIGNATURE, BODY));
    assertThrows(WebhookVerificationException.class, () -> VERIFIER.verify(ID, "-1", SIGNATURE, BODY));
    assertThrows(WebhookVerificationException.class, () -> VERIFIER.verify(ID, "1.5", SIGNATURE, BODY));
    assertThrows(WebhookVerificationException.class, () -> VERIFIER.verify(ID, TIMESTAMP, null, BODY));
    assertThrows(WebhookVerificationException.class, () -> VERIFIER.verify(ID, TIMESTAMP, SIGNATURE, null));
  }

  @Test
  void aSignedBodyThatIsntAnEventIsRefused() {
    for (String body : new String[] {"not json", "[]", "{}"}) {
      assertThrows(
          WebhookVerificationException.class,
          () -> VERIFIER.verify(ID, TIMESTAMP, sign(ID, TIMESTAMP, body), body),
          body);
    }
  }

  @Test
  void aNewEventTypeStillVerifiesAndReadsAsUnrecognized() {
    String body = BODY.replace("\"cdr-add\"", "\"cdr-update\"");
    WebhookEvent event = VERIFIER.verify(ID, TIMESTAMP, sign(ID, TIMESTAMP, body), body);
    assertEquals(WebhookEventType.UNRECOGNIZED, event.getType());
    assertEquals("cdr-update", event.getTypeValue());
    assertNull(event.getCdrAddEvent());
  }

  @Test
  void eachClassHasExactlyItsSchemaFields() {
    Map<String, Schema> schemas = PublicOpenAPI.WEBHOOK.model().getComponents().getSchemas();
    assertEquals(
        new TreeSet<>(schemas.get("CDRAddEvent").getProperties().keySet()), wireNames(WebhookEvent.class));
    assertEquals(
        new TreeSet<>(schemas.get("CDRAddData").getProperties().keySet()), wireNames(CdrAddEvent.class));
    assertEquals(
        "cdr-add",
        String.valueOf(((Schema<?>) schemas.get("CDRAddEvent").getProperties().get("type")).getEnum().get(0)));
  }

  private static TreeSet<String> wireNames(Class<?> type) {
    TreeSet<String> names = new TreeSet<>();
    for (Field field : type.getDeclaredFields()) {
      SerializedName name = field.getAnnotation(SerializedName.class);
      if (name != null) {
        names.add(name.value());
      }
    }
    return names;
  }

  /** Signs as UBIK does, with the vector's secret. */
  private static String sign(String id, String timestamp, String body) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(Base64.getDecoder().decode(SECRET.substring(6)), "HmacSHA256"));
      byte[] digest = mac.doFinal((id + "." + timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
      return "v1," + Base64.getEncoder().encodeToString(digest);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
