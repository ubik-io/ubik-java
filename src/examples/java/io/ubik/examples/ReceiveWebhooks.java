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

package io.ubik.examples;

import io.ubik.UBIK;
import io.ubik.cdr.CdrDetail;
import io.ubik.webhook.CdrAddEvent;
import io.ubik.webhook.WebhookEvent;
import io.ubik.webhook.WebhookEventType;
import io.ubik.webhook.WebhookVerificationException;
import io.ubik.webhook.WebhookVerifier;

/** Receive a cdr-add webhook: check it, then read the CDR it names. */
public final class ReceiveWebhooks {

  private ReceiveWebhooks() {}

  /**
   * Handles one request.
   *
   * @param secret the webhook's signing secret (whsec_...)
   * @param body the request body, exactly as received
   * @return the HTTP status to answer
   */
  public static int handle(
      UBIK client,
      String secret,
      String webhookId,
      String webhookTimestamp,
      String webhookSignature,
      String body) {
    // [START webhook-verify]
    WebhookVerifier verifier = WebhookVerifier.fromSecret(secret);
    WebhookEvent event;
    try {
      event = verifier.verify(webhookId, webhookTimestamp, webhookSignature, body);
    } catch (WebhookVerificationException e) {
      return 400; // not from UBIK, or too old
    }
    // [END webhook-verify]

    // [START webhook-test-event]
    if (event.getType() != WebhookEventType.CDR_ADD) {
      return 200; // a type this code doesn't handle: acknowledge it
    }
    String cdrId = event.getCdrAddEvent().getCdrId();
    if (CdrAddEvent.TEST_CDR_ID.equals(cdrId)) {
      return 200; // the test event from the webhook's page: no CDR
    }
    // [END webhook-test-event]

    CdrDetail cdr = client.cdrs().get(cdrId);
    System.out.println("Call completed: " + cdr.getId());
    return 200;
  }
}
