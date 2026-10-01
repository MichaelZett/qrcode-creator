package de.zettsystems.qrcreator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The version of this build, written into {@code version.properties} by {@code processResources}. Shown in the window
 * title so that a bug report can name it.
 */
final class AppVersion {
    static final String UNKNOWN = "dev";

    private AppVersion() {
    }

    static String get() {
        try (InputStream in = AppVersion.class.getResourceAsStream("version.properties")) {
            if (in == null) {
                return UNKNOWN;
            }
            Properties properties = new Properties();
            properties.load(in);
            String version = properties.getProperty("version", "");
            // an unfiltered resource (e.g. an IDE build without Gradle) still contains the placeholder
            return version.isBlank() || version.startsWith("$") ? UNKNOWN : version;
        } catch (IOException e) {
            return UNKNOWN;
        }
    }
}
