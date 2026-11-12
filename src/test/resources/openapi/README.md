# Public OpenAPI files

Copies of the UBIK API's public OpenAPI files, as published on the developer site (<https://ubik.io/en/developers/>). The tests check every request this library sends against them, and the test servers answer with their examples.

| File | API |
|---|---|
| auth.yaml | UBIK API authentication |

When a file changes on the developer site, copy the new version here and run `./mvnw verify`.
