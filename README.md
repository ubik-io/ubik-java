# ubik-java

Java library for the UBIK API: CDRs, call recordings and webhooks.

> **Not released yet.** This library is being built. Its API can still change until version 1.0.

## Requirements

- Java 11 or later.
- One runtime dependency: [Gson](https://github.com/google/gson) 2.10.1 or later, which itself depends on nothing.

## Usage

Create an API key for your organization in UBIK, and download its key file. Then create one client and share it:

```java
import io.ubik.ApiKeyCredentials;
import io.ubik.UBIK;
import io.ubik.UBIKOptions;
import java.io.FileInputStream;
import java.io.InputStream;

ApiKeyCredentials credentials;
try (InputStream in = new FileInputStream("/path/to/key-file.json")) {
  credentials = ApiKeyCredentials.fromStream(in);
}

UBIK client = UBIKOptions.newBuilder()
    .setOrgId("7d1f3a5c-2b4e-4f6a-8c9d-0e1f2a3b4c5d")
    .setCredentials(credentials)
    .build()
    .getService();
```

The client gets its access tokens itself, and gets a new one before each expires. It is safe to use from several threads.

Keep the key file secret: anyone who has it can call the API with the key's rights.

### Get a CDR and its recording

A cdr-add webhook gives you a CDR's id. Get the CDR, then save each recording's mixdown (every party in one MP3 file):

```java
CdrDetail cdr = client.cdrs().get(cdrId);
for (RecordingItem recording : cdr.getRecordingSessionList()) {
  try {
    client.recordings().downloadMixdownFile(
        cdr.getId(), recording.getMixdownFileId(), Paths.get(recording.getRecordingSessionId() + ".mp3"));
  } catch (RecordingNotReadyException e) {
    // Not ready yet: try again later.
  } catch (RecordingUnavailableException e) {
    // This file will never exist: don't retry.
  }
}
```

### List CDRs

Find the calls that ended while your webhook was inactive:

```java
ListCdrsRequest request = ListCdrsRequest.newBuilder()
    .setStartTimestampMilli(Operator.GREATER_OR_EQUAL, from)
    .setStartTimestampMilli(Operator.LESS, to)
    .build();
for (CdrItem cdr : client.cdrs().list(request).iterateAll()) {
  // every CDR of every page, newest first
}
```

A value the API adds later, for example a new disposition, reads as `UNRECOGNIZED`; `getDispositionValue()` gives the value as sent.

## Build

```bash
./mvnw verify
```

## Documentation

The UBIK API is documented at <https://ubik.io/en/developers/>.

## License

Apache License 2.0. See [LICENSE](LICENSE).
