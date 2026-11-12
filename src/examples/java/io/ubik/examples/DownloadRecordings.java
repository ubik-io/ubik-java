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
import io.ubik.cdr.RecordingChannel;
import io.ubik.cdr.RecordingDownload;
import io.ubik.cdr.RecordingItem;
import io.ubik.cdr.RecordingNotReadyException;
import io.ubik.cdr.RecordingUnavailableException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Download a CDR's recordings: the mixdown is one MP3 file with every party;
 * each channel is an Ogg/Opus file (mono, 48 kHz).
 */
public final class DownloadRecordings {

  private DownloadRecordings() {}

  /** Saves each recording's mixdown in folder. */
  public static void mixdowns(UBIK client, CdrDetail cdr, Path folder) throws IOException {
    // [START mixdown-download]
    for (RecordingItem recording : cdr.getRecordingSessionList()) {
      Path file = folder.resolve(recording.getRecordingSessionId() + ".mp3");
      try {
        client.recordings().downloadMixdownFile(cdr.getId(), recording.getMixdownFileId(), file);
      } catch (RecordingNotReadyException e) {
        // Not processed yet: try again later.
      } catch (RecordingUnavailableException e) {
        // This file will never exist: don't retry.
      }
    }
    // [END mixdown-download]
  }

  /** Saves each channel's file in folder. */
  public static void channels(UBIK client, CdrDetail cdr, Path folder) throws IOException {
    // [START channel-download]
    for (RecordingItem recording : cdr.getRecordingSessionList()) {
      for (RecordingChannel channel : recording.getChannelList()) {
        Path file = folder.resolve(channel.getChannelId() + ".ogg");
        try {
          client.recordings().downloadChannelFile(cdr.getId(), channel.getFileId(), file);
        } catch (RecordingNotReadyException e) {
          // Not processed yet: try again later.
        } catch (RecordingUnavailableException e) {
          // This file will never exist: don't retry.
        }
      }
    }
    // [END channel-download]
  }

  /** A link to a recording's mixdown, valid 5 minutes. */
  public static String mixdownLink(UBIK client, String cdrId, String mixdownFileId) {
    // [START mixdown-link]
    RecordingDownload link = client.recordings().getMixdownFileLink(cdrId, mixdownFileId);
    String url = link.getDownloadUrl(); // valid 5 minutes
    // [END mixdown-link]
    return url;
  }

  /** A link to one channel's file, valid 5 minutes. */
  public static String channelLink(UBIK client, String cdrId, String fileId) {
    // [START channel-link]
    RecordingDownload link = client.recordings().getChannelFileLink(cdrId, fileId);
    String url = link.getDownloadUrl(); // valid 5 minutes
    // [END channel-link]
    return url;
  }
}
