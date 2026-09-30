# qr-creator

Kleines Java-Tool zum Erzeugen von QR-Codes aus einem Text (z. B. URL) als PNG-Datei.

Ohne Argumente startet eine kleine Oberfläche: Typ per RadioButton wählen (URL/Text, E-Mail `mailto:`, Telefon `tel:`, WLAN), Daten eingeben, QR-Code ansehen, als PNG speichern.
Mit Argumenten arbeitet das Tool als Kommandozeilenprogramm (siehe unten).

## Download

Unter [Releases](../../releases) gibt es pro Betriebssystem (Windows, macOS, Linux) ein ZIP mit
einem eigenständigen Programm inkl. minimaler Java-Laufzeit – Java muss nicht installiert sein.
ZIP entpacken und `QR-Creator` (`QR-Creator.exe` unter Windows) starten.

Gebaut wird mit `jpackage` (Teil des JDK) – das geht nur für das jeweilige Host-System, daher
baut die GitHub-Actions-Matrix (`.github/workflows/build.yml`) auf Linux, Windows und macOS.
Lokal: `./gradlew appImage` → `build/app-image/`. 
Release-Prozess:

1. In `gradle.properties` den Suffix `-SNAPSHOT` von `appVersion` entfernen.
2. In `CHANGELOG.md` den Abschnitt `## <appVersion> - <Datum>` anlegen (wird zum Release-Text).
3. Auf `main` pushen: Die CI testet, baut die drei ZIPs, taggt `v<appVersion>` und erstellt das GitHub-Release.
4. Danach setzt die CI `appVersion` selbst auf die nächste `-SNAPSHOT`-Version (`[skip ci]`) – vor dem Weiterarbeiten `git pull`.

Bei `-SNAPSHOT`-Versionen wird nur getestet, analysiert und das Paketieren geprüft – es entsteht kein Release.

## SonarCloud

Analyse läuft in der CI (`./gradlew sonar`). Einrichtung: Projekt `MichaelZett_qrcode-creator`
in Organisation `michaelzett` auf sonarcloud.io anlegen, „Automatic Analysis“ deaktivieren und
das Token als Repository-Secret `SONAR_TOKEN` hinterlegen. Abweichende Keys in `build.gradle` (`sonar { }`) anpassen.

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
