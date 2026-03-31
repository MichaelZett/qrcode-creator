# qr-creator

Kleines Java-Tool zum Erzeugen von QR-Codes aus einem Text (z. B. URL) als PNG-Datei.

## Voraussetzungen

- Java 25
- Gradle Wrapper (`gradlew` / `gradlew.bat`)

## Starten

Default-Werte verwenden (Text + Ausgabedatei):

```powershell
.\gradlew run
```

Mit eigenen Werten:

```powershell
.\gradlew run --args="https://example.com files\mein-qr.png"
```

Ergebnisdateien liegen standardmäßig im Ordner `files/`.

## `mailto`-Link

Ein einfacher `mailto`-Link:

```text
mailto:max.mustermann@example.com
```

Mit Betreff und Text:

```text
mailto:max.mustermann@example.com?subject=Anfrage&body=Hallo%20Max%2C%0Aich%20habe%20eine%20Frage.
```

Wichtig: Sonderzeichen und Leerzeichen URL-encoden (z. B. Leerzeichen = `%20`, Zeilenumbruch = `%0A`).
