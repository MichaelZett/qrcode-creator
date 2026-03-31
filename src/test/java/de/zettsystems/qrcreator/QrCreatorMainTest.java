package de.zettsystems.qrcreator;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QrCreatorMainTest {

    @Test
    void shouldUseDefaultInputWhenNoArgsAreProvided() {
        assertEquals(QrCreatorMain.DEFAULT_INPUT, QrCreatorMain.resolveInput(new String[0]));
    }

    @Test
    void shouldUseFirstArgAsInput() {
        assertEquals("hello", QrCreatorMain.resolveInput(new String[]{"hello"}));
    }

    @Test
    void shouldUseDefaultOutputWhenNoSecondArgIsProvided() {
        assertEquals(Path.of(QrCreatorMain.DEFAULT_OUTPUT_FILE), QrCreatorMain.resolveOutputFile(new String[]{"hello"}));
    }

    @Test
    void shouldUseSecondArgAsOutputPath() {
        assertEquals(Path.of("out.png"), QrCreatorMain.resolveOutputFile(new String[]{"hello", "out.png"}));
    }
}
