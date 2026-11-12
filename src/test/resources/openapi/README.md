# Public OpenAPI files

Copies of the OpenAPI files the developer site publishes (<https://ubik.io/en/developers/>). The tests check every request this library sends against them, and the test servers answer with their examples.

| File | API | Published at |
|---|---|---|
| authentication.yaml | UBIK API authentication | <https://ubik.io/developers/openapi/authentication.yaml> |
| call-history.yaml | UBIK API call history | <https://ubik.io/developers/openapi/call-history.yaml> |
| webhooks.yaml | UBIK webhooks | <https://ubik.io/developers/openapi/webhooks.yaml> |

The API drift workflow runs the tests against the published files every day. When a file changes, copy the new version here and run `./mvnw verify`.
