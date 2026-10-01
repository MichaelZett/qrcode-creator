# Contributing

Thanks for looking. QR-Creator is a small tool that does one thing, so the most valuable contribution is usually a
precise bug report, and the second most valuable is a small, focused pull request.

**Security flaws do not belong here.** See [SECURITY.md](SECURITY.md).

## Before you write code

Open an issue first for anything beyond an obvious fix. It costs you nothing and it avoids the outcome nobody wants:
a finished pull request that has to be turned down because it conflicts with a rule below.

## What this tool will not do

A change that breaks one of these rules will not be merged, however well it is written.

- **No network.** No update check, no telemetry, no online lookup. The program never opens a connection, as
  [PRIVACY.md](PRIVACY.md) promises.
- **Nothing is stored that the user did not ask for.** Input and images that were read are not saved or logged; a
  file is written only when the user saves one.
- **No dependency that is not really needed.** Every library ends up in every download. The packages bundle a Java
  runtime reduced to `java.desktop`; a library that needs further JDK modules also makes every download larger.
- **The UI stays bilingual.** Every text goes through `Messages` and exists in both `messages_de.properties` and
  `messages_en.properties`; `MessagesTest` checks that the keys match.
- **The command line stays compatible.** `CONTENT OUTPUT_FILE` and `--decode IMAGE_FILE` may be used in scripts —
  extend it, do not change what existing arguments mean.

## Building

You need **Java 25**.

```
./gradlew build
```

compiles and runs the tests. `./gradlew run` starts the UI, `./gradlew appImage` builds the self-contained package
for your operating system under `build/app-image/`.

## House rules for a pull request

- **Tests come with the change.** A bug fix carries a test that fails without it. The Swing UI cannot be tested
  headless, so keep logic out of `QrCreatorApp` and in classes that can be tested.
- **Comments explain why, not what.**
- **English** for code, comments and documentation.
- **Add a `CHANGELOG.md` entry** under `## Unreleased` when the change is visible to users.
- Keep the pull request to one subject. Unrelated cleanups make a change harder to review and to revert.

Releases and version numbers are handled by the maintainer — please leave `appVersion` in `gradle.properties` alone.
