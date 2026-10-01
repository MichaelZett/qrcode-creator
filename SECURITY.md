# Security Policy

QR-Creator works offline, but it reads images you give it and is distributed as a ready-to-run program. A flaw in
either can harm the computer it runs on. Please treat findings accordingly.

## Reporting a vulnerability

**Do not open a public issue.** A public issue publishes the flaw before a fix exists, which is exactly the situation
a fix is supposed to prevent.

Use GitHub's private vulnerability reporting instead: go to the
[Security tab](https://github.com/MichaelZett/qrcode-creator/security) and choose **Report a vulnerability**. The
report stays visible only to you and the maintainer until an advisory is published.

If you cannot use that form, contact the maintainer through the address on the
[GitHub profile](https://github.com/MichaelZett) and say only that you have a security report — no details in that
first message.

### What helps

- The affected version, the operating system, and whether you used a release package or built it yourself.
- Which entry point is involved: reading an image (file, clipboard, drag and drop, `--decode`), creating a code,
  saving the PNG, or the release downloads themselves.
- A minimal way to reproduce it — for a crafted image, the image itself.
- What an attacker gains — code execution, reading or overwriting files, a download that passes verification but is
  not genuine.

## What to expect

This is a single-maintainer project. There is no bug bounty and no guaranteed response window. What is promised:

- An acknowledgement that the report arrived.
- An honest assessment of whether it is a vulnerability, and why.
- A fix released as a new version, with the issue named in `CHANGELOG.md`, and credit in the advisory unless you
  prefer otherwise.

Please give a fix a reasonable chance before disclosing publicly.

## Supported versions

Only the latest release receives fixes. Older versions are not patched — the upgrade path is forward.

| Version | Supported |
|---------|-----------|
| latest  | yes       |
| older   | no        |

## Out of scope

- The Windows SmartScreen or macOS Gatekeeper warning. The packages are not code-signed, as the README states; verify
  a download with the signed checksums instead.
- Content of a QR code. QR-Creator shows the text of a code, it does not open links or join networks — what you do
  with the text is up to you.
- Findings that require an already compromised computer or user account.
- Reports produced by a scanner without a demonstrated impact on this program.
