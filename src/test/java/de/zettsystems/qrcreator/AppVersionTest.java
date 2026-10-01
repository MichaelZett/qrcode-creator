package de.zettsystems.qrcreator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppVersionTest {

    @Test
    void shouldReadVersionWrittenByTheBuild() {
        String version = AppVersion.get();

        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"), version);
    }
}
