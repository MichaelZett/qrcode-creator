# ZettSystems QR-Creator

Small Java tool that creates QR codes (URL/text, email, phone, Wi-Fi) as PNG files.

Without arguments it opens a small UI: pick the type via radio button (URL/Text, email `mailto:`, phone `tel:`,
Wi-Fi), enter the data, look at the QR code, save it as PNG. The UI is available in German and English; the
language follows the system language and can be switched at the top right.
With arguments the tool runs as a command-line program (see below).

## Download

[Releases](../../releases) offer one ZIP per operating system (Windows, macOS, Linux) containing a self-contained
program including a minimal Java runtime – Java does not need to be installed.
Unzip and start `QR-Creator` (`QR-Creator.exe` on Windows). Each release also has a `SHA256SUMS` file (and
`SHA256SUMS.asc` if a GPG key is configured) to verify the downloads.

Code signing policy: the Windows executable is signed with a certificate provided by the
[SignPath Foundation](https://signpath.org), free code signing by [SignPath.io](https://signpath.io) (see *Signing*).
The macOS package is not signed or notarized, so Gatekeeper may block it.

Packages are built with `jpackage` (part of the JDK), which only works for the host system, so the GitHub Actions
matrix (`.github/workflows/build.yml`) builds on Linux, Windows and macOS.
Locally: `./gradlew appImage` → `build/app-image/`.

## Release process

1. Remove the `-SNAPSHOT` suffix from `appVersion` in `gradle.properties`.
2. Add a section `## <appVersion> - <date>` to `CHANGELOG.md` (it becomes the release text).
3. Push to `main`: CI tests, analyzes, builds the three ZIPs, tags `v<appVersion>` and creates the GitHub release.
4. CI then sets `appVersion` to the next `-SNAPSHOT` version (`[skip ci]`) – run `git pull` before continuing.

For `-SNAPSHOT` versions CI only tests, analyzes and checks that packaging works – no release is created.

## Signing

Signing is optional and only happens for releases. Without the configuration the steps are skipped.

- **Windows** (`QR-Creator.exe`): signed through [SignPath](https://signpath.io), which is free for open-source
  projects. The private key stays in SignPath's HSM. Setup:
  1. Apply for a free certificate at [signpath.org](https://signpath.org) (the SignPath Foundation), using this
     repository and its Apache-2.0 license.
  2. In SignPath create the project, an artifact configuration from
     [`.signpath/artifact-configuration.xml`](.signpath/artifact-configuration.xml) and a release signing policy;
     add the GitHub.com trusted build system and install the SignPath GitHub App.
  3. In the repository set the secret `SIGNPATH_API_TOKEN` and the variables `SIGNPATH_ORGANIZATION_ID`,
     `SIGNPATH_PROJECT_SLUG` and `SIGNPATH_SIGNING_POLICY_SLUG`:

     ```powershell
     gh secret set SIGNPATH_API_TOKEN
     gh variable set SIGNPATH_ORGANIZATION_ID
     gh variable set SIGNPATH_PROJECT_SLUG
     gh variable set SIGNPATH_SIGNING_POLICY_SLUG
     ```

  Free code signing provided by [SignPath.io](https://signpath.io), certificate by
  [SignPath Foundation](https://signpath.org).
- **Linux** has no OS-level signing. The release always contains `SHA256SUMS`. If the secrets `GPG_PRIVATE_KEY`
  (ASCII-armored private key) and `GPG_PASSPHRASE` are set, `SHA256SUMS.asc` (detached signature) is added.
- **macOS** is not signed yet (needs an Apple Developer account, `jpackage --mac-sign` and notarization).

## SonarCloud

The analysis runs in CI (`./gradlew sonar`). Setup: create the project `MichaelZett_qrcode-creator` in the
organization `michaelzett` on sonarcloud.io, disable "Automatic Analysis" and store the token as repository secret
`SONAR_TOKEN`. Adjust the keys in `build.gradle` (`sonar { }`) if they differ.

## Requirements

- Java 25
- Gradle wrapper (`gradlew` / `gradlew.bat`)

## Run

```powershell
.\gradlew run
```

starts the UI. With arguments it creates a PNG on the command line:

```powershell
.\gradlew run --args="https://example.com files\my-qr.png"
```

## Command line

```powershell
.\gradlew run --args="CONTENT OUTPUT_FILE"
```

The output file is set by the **second parameter**:

```powershell
.\gradlew run --args="https://example.com files\my-qr.png"
.\gradlew run --args="mailto:max@example.com files\contact.png"
```

Defaults if a parameter is missing: content `https://www.tg-heimfeld.com/`, output file `files/tgh.png`.

## `mailto` links

A simple `mailto` link:

```text
mailto:max.mustermann@example.com
```

With subject and body:

```text
mailto:max.mustermann@example.com?subject=Request&body=Hello%20Max%2C%0AI%20have%20a%20question.
```

Important: URL-encode special characters and spaces (e.g. space = `%20`, line break = `%0A`).

## License

[Apache-2.0](LICENSE)

## Privacy

The program works offline and does not collect or transmit any data, see [PRIVACY.md](PRIVACY.md).
