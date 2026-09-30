# Changelog

## 1.0.1 - 2026-09-30

- The UI is available in German and English (follows the system language, switchable in the window).
- The UI no longer pre-fills a URL at startup.
- A directory as output file is now rejected with a clear error instead of being silently replaced.
- Releases include `SHA256SUMS` (and a GPG signature if configured) to verify the downloads.
- Added the Apache-2.0 license and a privacy policy (the program works offline and collects no data).
- Windows code signing via SignPath is prepared but not active yet.
- More unit tests; English documentation.

## 1.0.0 - 2026-09-30

- Swing UI for creating QR codes (URL/text, email, phone, Wi-Fi) with PNG export.
- Command-line mode with content and output file as arguments.
- Self-contained packages (with a minimal Java runtime) for Windows, macOS and Linux.
