# ZettSystems QR-Creator

Small Java tool that creates QR codes (URL/text, email, phone, Wi-Fi) as PNG files and reads the text of existing
QR codes from images.

Without arguments it opens a small UI: pick the type via radio button (URL/Text, email `mailto:`, phone `tel:`,
Wi-Fi), enter the data, look at the QR code, save it as PNG. The UI is available in German and English; the
language follows the system language and can be switched at the top right.

To read a QR code, use the row at the bottom: *Open image …* (PNG, JPEG, GIF, BMP, TIFF), *From clipboard* (e.g. a
screenshot or a file copied in the file manager) or drop the image onto the window. The encoded text appears in a
dialog and can be copied. WebP and HEIC are not supported – convert them to PNG or JPEG first.
With arguments the tool runs as a command-line program (see below).

## Download

[Releases](../../releases) offer one ZIP per operating system (Windows, macOS, Linux) containing a self-contained
program including a minimal Java runtime – Java does not need to be installed.
Unzip and start `QR-Creator` (`QR-Creator.exe` on Windows). Each release also has a `SHA256SUMS` file (and
`SHA256SUMS.asc` if a GPG key is configured) to verify the downloads.

The Windows executable is not code-signed (see *Signing*), so Windows SmartScreen may warn on the first start –
choose *More info* → *Run anyway*. The macOS package is not signed or notarized either, so Gatekeeper may block it.
To be sure a download is genuine, verify it with the signed checksums (see *Verifying downloads*).

Packages are built with `jpackage` (part of the JDK), which only works for the host system, so the GitHub Actions
matrix (`.github/workflows/build.yml`) builds on Linux, Windows and macOS.
Locally: `./gradlew appImage` → `build/app-image/`.

## Verifying downloads

Every release contains `SHA256SUMS` with the checksums of the ZIP files. Releases that also have `SHA256SUMS.asc`
carry a GPG signature over that file, made with the key of Michael Zöller:

```text
037B 45BA 4CDD ACE9 8BEA  FEDC B44D DFE4 D21B 7E6C
```

1. Download the ZIP, `SHA256SUMS` and `SHA256SUMS.asc` into one folder.
2. Import the public key and check the signature:

   ```powershell
   gpg --keyserver hkps://keys.openpgp.org --recv-keys 037B45BA4CDDACE98BEAFEDCB44DDFE4D21B7E6C
   gpg --verify SHA256SUMS.asc SHA256SUMS
   ```

   `Good signature from "Michael Zöller <michael2.zoeller@gmail.com>"` means the file is genuine. The fingerprint
   printed by `gpg` must match the one above (a warning that the key is not certified is normal for a key you have
   not signed yourself).
3. Compare the checksum of your download with the signed list:

   ```powershell
   # Windows
   (Get-FileHash qr-creator-windows.zip -Algorithm SHA256).Hash.ToLower()
   ```

   ```bash
   # Linux: checks every file from the list that is present
   sha256sum --ignore-missing -c SHA256SUMS

   # macOS: prints the hash of the ZIP
   shasum -a 256 qr-creator-macos.zip
   ```

   On Windows and macOS the printed hash must equal the line for your ZIP in `SHA256SUMS`.

## Release process

1. Remove the `-SNAPSHOT` suffix from `appVersion` in `gradle.properties`.
2. Add a section `## <appVersion> - <date>` to `CHANGELOG.md` (it becomes the release text).
3. Push to `main`: CI tests, analyzes, builds the three ZIPs, tags `v<appVersion>` and creates the GitHub release.
4. CI then sets `appVersion` to the next `-SNAPSHOT` version (`[skip ci]`) – run `git pull` before continuing.

For `-SNAPSHOT` versions CI only tests, analyzes and checks that packaging works – no release is created.

## Signing

Signing is optional and only happens for releases. Without the configuration the steps are skipped.

- **Windows** (`QR-Creator.exe`): **not signed.** The application to the SignPath Foundation for a free certificate
  was declined. The build still contains an optional [SignPath](https://signpath.io) step that stays skipped without
  the configuration below, so it can be used if a certificate becomes available. Setup:
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

Reading a QR code prints the encoded text (one line per code found):

```powershell
.\gradlew run --args="--decode files\my-qr.png"
```

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

## Feedback and contributing

Found a bug or missing something? Open an [issue](../../issues/new/choose) – the forms ask for the version and the
operating system. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before a pull request.
**Security flaws** go through private reporting, not a public issue, see [SECURITY.md](SECURITY.md).

## License

[Apache-2.0](LICENSE)

## Privacy

The program works offline and does not collect or transmit any data, see [PRIVACY.md](PRIVACY.md).
