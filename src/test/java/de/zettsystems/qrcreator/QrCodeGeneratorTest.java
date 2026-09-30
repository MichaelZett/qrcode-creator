package de.zettsystems.qrcreator;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeGeneratorTest {

    private final QrCodeGenerator generator = new QrCodeGenerator();

    @BeforeAll
    static void useEnglish() {
        Messages.setLocale(Locale.ENGLISH);
    }

    @Test
    void shouldGenerateQrImageForValidInput() {
        BufferedImage image = generator.generate("https://zett.systems", 256, 256);

        assertNotNull(image);
        assertEquals(256, image.getWidth());
        assertEquals(256, image.getHeight());
    }

    @Test
    void shouldGenerateImageThatDecodesToTheInput() throws Exception {
        assertEquals("https://zett.systems", decode(generator.generate("https://zett.systems", 300, 300)));
    }

    @Test
    void shouldRejectBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(" ", 128, 128));
    }

    @Test
    void shouldRejectNullInput() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(null, 128, 128));
    }

    @Test
    void shouldRejectNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("abc", 0, 128));
        assertThrows(IllegalArgumentException.class, () -> generator.generate("abc", 128, -1));
    }

    @Test
    void shouldWrapEncodingFailureForTooLongInput() {
        String tooLong = "x".repeat(8000);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> generator.generate(tooLong, 300, 300));

        assertEquals(Messages.get("error.encode"), ex.getMessage());
    }

    @Test
    void shouldWritePngFileThatDecodesToTheInput(@TempDir Path dir) throws Exception {
        Path target = dir.resolve("qr.png");

        generator.writeToFile("hello", 200, 200, target);

        assertEquals("hello", decode(ImageIO.read(target.toFile())));
    }

    @Test
    void shouldCreateMissingParentDirectories(@TempDir Path dir) {
        Path target = dir.resolve("a").resolve("b").resolve("qr.png");

        generator.writeToFile("hello", 100, 100, target);

        assertTrue(Files.isRegularFile(target));
    }

    @Test
    void shouldRejectDirectoryAsTargetAndKeepIt(@TempDir Path dir) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> generator.writeToFile("hello", 100, 100, dir));

        assertEquals(Messages.get("error.directory", dir), ex.getMessage());
        assertTrue(Files.isDirectory(dir));
    }

    @Test
    void shouldRejectNonEmptyDirectoryAsTarget(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("keep.txt"), "x");

        assertThrows(IllegalArgumentException.class, () -> generator.writeToFile("hello", 100, 100, dir));
    }

    @Test
    void shouldWrapIoFailureWhenParentIsAFile(@TempDir Path dir) throws IOException {
        Path file = Files.writeString(dir.resolve("file.txt"), "x");
        Path target = file.resolve("qr.png");

        assertThrows(IllegalStateException.class, () -> generator.writeToFile("hello", 100, 100, target));
    }

    private static String decode(BufferedImage image) throws NotFoundException, FormatException, ChecksumException {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        return new QRCodeReader().decode(bitmap).getText();
    }
}
