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

## Output-Dateiname festlegen

Der Output-Name wird über den **2. Parameter** gesetzt:

```powershell
.\gradlew run --args="INHALT DATEIPFAD"
```

Beispiele:

```powershell
.\gradlew run --args="https://example.com files\mein-qr.png"
.\gradlew run --args="mailto:max@example.com files\kontakt.png"
```

Wenn kein 2. Parameter gesetzt ist, wird der Default verwendet:

```text
files/tennis.png
```

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
