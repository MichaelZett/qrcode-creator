package de.zettsystems.qrcreator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QrCreatorMainTest {

    @Test
    void shouldUseDefaultInputWhenNoArgsAreProvided() {
        assertEquals(QrCreatorMain.DEFAULT_INPUT, QrCreatorMain.resolveInput(new String[0]));
    }

    @Test
    void shouldUseDefaultInputWhenArgsAreNull() {
        assertEquals(QrCreatorMain.DEFAULT_INPUT, QrCreatorMain.resolveInput(null));
    }

    @Test
    void shouldUseDefaultInputWhenFirstArgIsBlankOrNull() {
        assertEquals(QrCreatorMain.DEFAULT_INPUT, QrCreatorMain.resolveInput(new String[]{" "}));
        assertEquals(QrCreatorMain.DEFAULT_INPUT, QrCreatorMain.resolveInput(new String[]{null}));
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
    void shouldUseDefaultOutputWhenArgsAreNullOrSecondArgIsBlankOrNull() {
        Path expected = Path.of(QrCreatorMain.DEFAULT_OUTPUT_FILE);

        assertEquals(expected, QrCreatorMain.resolveOutputFile(null));
        assertEquals(expected, QrCreatorMain.resolveOutputFile(new String[]{"hello", " "}));
        assertEquals(expected, QrCreatorMain.resolveOutputFile(new String[]{"hello", null}));
    }

    @Test
    void shouldUseSecondArgAsOutputPath() {
        assertEquals(Path.of("out.png"), QrCreatorMain.resolveOutputFile(new String[]{"hello", "out.png"}));
    }

    @Test
    void shouldWriteQrCodeFileInCommandLineMode(@TempDir Path dir) throws IOException {
        Path target = dir.resolve("cli.png");

        QrCreatorMain.main(new String[]{"hello", target.toString()});

        assertTrue(Files.size(target) > 0);
    }

    @Test
    void shouldPrintDecodedTextInCommandLineMode(@TempDir Path dir) {
        Path image = dir.resolve("cli.png");
        QrCreatorMain.main(new String[]{"hello", image.toString()});
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        QrCreatorMain.decode(new String[]{QrCreatorMain.DECODE_OPTION, image.toString()},
                new PrintStream(out, true, StandardCharsets.UTF_8));

        assertEquals("hello", out.toString(StandardCharsets.UTF_8).strip());
    }

    @Test
    void shouldRequireFileForDecodeOption() {
        assertThrows(IllegalArgumentException.class, () -> QrCreatorMain.main(new String[]{QrCreatorMain.DECODE_OPTION}));
        assertThrows(IllegalArgumentException.class,
                () -> QrCreatorMain.main(new String[]{QrCreatorMain.DECODE_OPTION, " "}));
    }
}
