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

import io.ubik.UBIKException;
import io.ubik.internal.Transport;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The files of a CDR's recordings. Get it with {@code client.recordings()}.
 *
 * <p>A recording has a mixdown (every party in one MP3 file,
 * {@link RecordingItem#getMixdownFileId()}) and one file per channel
 * ({@link RecordingChannel#getFileId()}). The API key needs the right
 * "Call history: Read".
 */
public final class Recordings {

  private static final String RECORDING_IS = "recording is ";

  private final Transport transport;

  /** Internal: get this service with {@code client.recordings()}. */
  public Recordings(Transport transport) {
    this.transport = transport;
  }

  /**
   * A link to the recording's mixdown, valid 5 minutes. Ask for a new one
   * each time.
   *
   * @throws RecordingNotReadyException when the file isn't ready yet: try again later
   * @throws RecordingUnavailableException when the file will never exist: don't retry
   * @throws io.ubik.NotFoundException when the CDR or the file doesn't exist
   */
  public RecordingDownload getMixdownFileLink(String cdrId, String fileId) {
    return getLink("mixdown", cdrId, fileId);
  }

  /**
   * A link to one channel's file, valid 5 minutes. Ask for a new one each
   * time.
   *
   * @throws RecordingNotReadyException when the file isn't ready yet: try again later
   * @throws RecordingUnavailableException when the file will never exist: don't retry
   * @throws io.ubik.NotFoundException when the CDR or the file doesn't exist
   */
  public RecordingDownload getChannelFileLink(String cdrId, String fileId) {
    return getLink("channel", cdrId, fileId);
  }

  /**
   * Writes the recording's mixdown (MP3) to a file, replacing it when it
   * exists. Asks for a new link each time.
   *
   * @throws RecordingNotReadyException when the file isn't ready yet: try again later
   * @throws RecordingUnavailableException when the file will never exist: don't retry
   * @throws io.ubik.NotFoundException when the CDR or the file doesn't exist
   * @throws IOException when the file can't be written
   */
  public void downloadMixdownFile(String cdrId, String fileId, Path path) throws IOException {
    downloadTo(getMixdownFileLink(cdrId, fileId), path);
  }

  /**
   * Writes the recording's mixdown (MP3) to out, without closing it. Asks
   * for a new link each time.
   *
   * @throws RecordingNotReadyException when the file isn't ready yet: try again later
   * @throws RecordingUnavailableException when the file will never exist: don't retry
   * @throws io.ubik.NotFoundException when the CDR or the file doesn't exist
   */
  public void downloadMixdownFile(String cdrId, String fileId, OutputStream out) {
    transport.download(getMixdownFileLink(cdrId, fileId).getDownloadUrl(), out);
  }

  /**
   * Writes one channel's file to a file, replacing it when it exists. Asks
   * for a new link each time.
   *
   * @throws RecordingNotReadyException when the file isn't ready yet: try again later
   * @throws RecordingUnavailableException when the file will never exist: don't retry
   * @throws io.ubik.NotFoundException when the CDR or the file doesn't exist
   * @throws IOException when the file can't be written
   */
  public void downloadChannelFile(String cdrId, String fileId, Path path) throws IOException {
    downloadTo(getChannelFileLink(cdrId, fileId), path);
  }

  /**
   * Writes one channel's file to out, without closing it. Asks for a new
   * link each time.
   *
   * @throws RecordingNotReadyException when the file isn't ready yet: try again later
   * @throws RecordingUnavailableException when the file will never exist: don't retry
   * @throws io.ubik.NotFoundException when the CDR or the file doesn't exist
   */
  public void downloadChannelFile(String cdrId, String fileId, OutputStream out) {
    transport.download(getChannelFileLink(cdrId, fileId).getDownloadUrl(), out);
  }

  private void downloadTo(RecordingDownload link, Path path) throws IOException {
    Objects.requireNonNull(path, "path");
    try (OutputStream out = Files.newOutputStream(path)) {
      transport.download(link.getDownloadUrl(), out);
    } catch (UBIKException e) {
      Files.deleteIfExists(path);
      throw e;
    }
  }

  private RecordingDownload getLink(String kind, String cdrId, String fileId) {
    Objects.requireNonNull(cdrId, "cdrId");
    Objects.requireNonNull(fileId, "fileId");
    HttpResponse<String> response =
        transport.get(
            "/api/v1/cdr/"
                + Transport.encode(cdrId)
                + "/recording/dl/"
                + kind
                + "/"
                + Transport.encode(fileId),
            Cdrs.scope(transport.getOrgId()));
    if (response.statusCode() == 409) {
      throw notProcessed(response);
    }
    Transport.check(response);
    RecordingDownload link = Cdrs.parse(response, RecordingDownload.class);
    if (link.getDownloadUrl() == null || link.getDownloadUrl().isEmpty()) {
      throw new UBIKException("The response has no download_url", response.statusCode(), null);
    }
    return link;
  }

  private static UBIKException notProcessed(HttpResponse<String> response) {
    String body = response.body() == null ? "" : response.body().trim();
    String state = body.startsWith(RECORDING_IS) ? body.substring(RECORDING_IS.length()) : "";
    String message = "HTTP 409: " + body;
    switch (state) {
      case "IN_PROGRESS":
      case "RECORDING_COMPLETE":
        return new RecordingNotReadyException(message, state);
      case "FAILED_TOO_MANY_RETRIES":
      case "EXPIRED":
        return new RecordingUnavailableException(message, state);
      default:
        return new UBIKException(message, 409, null);
    }
  }
}
