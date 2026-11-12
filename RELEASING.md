# Releasing

A tag `vX.Y.Z` on `main` publishes that version to Maven Central (`io.ubik:ubik-java`), through `.github/workflows/release.yml`:

1. The tests run on Java 11, 17, 21 and 25.
2. The version is set from the tag.
3. The jar, sources and Javadoc are signed and published to Maven Central.
4. A GitHub release is made, with the notes generated from the merged pull requests.

A published version can't be deleted or replaced: fix a mistake with a new version.

## One-time setup

| Step | Where |
|---|---|
| Create an account on the Central Portal | <https://central.sonatype.com> |
| Verify the namespace `io.ubik`: add the DNS TXT record the portal gives to `ubik.io` | Central Portal, Namespaces |
| Generate a user token | Central Portal, Account |
| Create a GPG key for signing, and publish its public key to keys.openpgp.org | Your machine |
| Add the repository secrets `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` (the token), `GPG_PRIVATE_KEY` (armored), `GPG_PASSPHRASE` | GitHub, Settings, Secrets and variables, Actions |

## Check the setup

Run the **Release check** workflow (Actions, Release check, Run workflow) after the setup and after changing a secret. It signs and verifies the jars and checks the Central token, without publishing anything.

## Each release

1. `main` is green, including the API drift job and the Release check.
2. Pick the version: while it is 0.x, a change to the library's API raises the middle number.
3. Tag `main` and push the tag: `git tag vX.Y.Z && git push origin vX.Y.Z`.
4. Check that the Release workflow is green and that the version shows on <https://central.sonatype.com/artifact/io.ubik/ubik-java>.
5. Ask for the developer docs update: they vendor `docs-examples-vX.Y.Z.zip` from the GitHub release.

## Docs version

`.github/workflows/docs-version.yml` checks that the developer docs' Java install page shows the latest release. It runs by hand until the install page is live; then its daily schedule is turned on. When it is red, update the developer docs to the latest tag.

## API drift

`.github/workflows/drift.yml` runs every day: it downloads the OpenAPI files the developer site publishes and runs the tests against them. When it is red, copy the new files into `src/test/resources/openapi`, update the library, and open a pull request.
