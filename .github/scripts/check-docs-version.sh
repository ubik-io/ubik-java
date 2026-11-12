#!/usr/bin/env bash
# Fails when the developer docs' Java install page shows another version than
# the latest ubik-java release on Maven Central.
set -euo pipefail

# The install page: it shows the version in one <code data-sdk-version> element.
DOCS_URL="${DOCS_URL:-https://ubik.io/en/developers/sdk/java/}"
# Maven Central's metadata for io.ubik:ubik-java.
METADATA_URL="${METADATA_URL:-https://repo1.maven.org/maven2/io/ubik/ubik-java/maven-metadata.xml}"

page="$(curl --fail --silent --show-error --location "$DOCS_URL")"
count="$( (grep -o 'data-sdk-version[^>]*>[^<]*<' <<<"$page" || true) | wc -l | tr -d ' ')"
if [ "$count" != 1 ]; then
  echo "The docs page has $count <code data-sdk-version> elements, not 1: $DOCS_URL" >&2
  exit 1
fi
docs="$(grep -o 'data-sdk-version[^>]*>[^<]*<' <<<"$page" | sed -E 's/.*>([^<]*)<$/\1/')"

latest="$(curl --fail --silent --show-error --location "$METADATA_URL" | grep -o '<release>[^<]*</release>' | sed -E 's|</?release>||g')"
if [ -z "$latest" ]; then
  echo "No <release> in $METADATA_URL" >&2
  exit 1
fi

echo "Docs: $docs. Maven Central: $latest."
if [ "$docs" != "$latest" ]; then
  echo "The docs show $docs but the latest release is $latest: update the developer docs (sync-sdk-java.sh at v$latest)." >&2
  exit 1
fi
