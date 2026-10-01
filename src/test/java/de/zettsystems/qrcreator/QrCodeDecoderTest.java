package de.zettsystems.qrcreator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeDecoderTest {

    private final QrCodeGenerator generator = new QrCodeGenerator();
    private final QrCodeDecoder decoder = new QrCodeDecoder();

    @BeforeAll
    static void useEnglish() {
        Messages.setLocale(Locale.ENGLISH);
    }

    @Test
    void shouldDecodeGeneratedImage() {
        assertEquals(List.of("https://zett.systems"), decoder.decode(generator.generate("https://zett.systems", 300, 300)));
    }

    @Test
    void shouldKeepUmlautsAndSymbols() {
        assertEquals(List.of("Grüße € 😀"), decoder.decode(generator.generate("Grüße € 😀", 300, 300)));
    }

    @Test
    void shouldDecodeWifiPayload() {
        String wifi = QrPayload.wifi("Home;Net", "se:cret", "WPA");

        assertEquals(List.of(wifi), decoder.decode(generator.generate(wifi, 300, 300)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"png", "jpg", "gif", "bmp", "tiff"})
    void shouldDecodeCommonImageFormats(String format, @TempDir Path dir) throws IOException {
        Path file = dir.resolve("qr." + format);
        // JPEG and BMP have no alpha channel and do not accept every image type, plain RGB works everywhere
        assertTrue(ImageIO.write(onCanvas(generator.generate("hello " + format, 300, 300), 300, 300, 0, 0), format,
                file.toFile()));

        assertEquals(List.of("hello " + format), decoder.decode(file));
    }

    @Test
    void shouldDecodeCodeInsideLargerPicture() {
        BufferedImage picture = onCanvas(generator.generate("inside", 200, 200), 800, 600, 350, 250);

        assertEquals(List.of("inside"), decoder.decode(picture));
    }

    @Test
    void shouldDecodeSeveralCodesInOneImage() {
        BufferedImage picture = new BufferedImage(700, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = picture.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 700, 300);
        g.drawImage(generator.generate("first", 300, 300), 0, 0, null);
        g.drawImage(generator.generate("second", 300, 300), 400, 0, null);
        g.dispose();

        assertEquals(List.of("first", "second"), decoder.decode(picture).stream().sorted().toList());
    }

    @Test
    void shouldDecodeInvertedCode() {
        BufferedImage image = onCanvas(generator.generate("inverted", 300, 300), 300, 300, 0, 0);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, ~image.getRGB(x, y) | 0xFF000000);
            }
        }

        assertEquals(List.of("inverted"), decoder.decode(image));
    }

    @Test
    void shouldRejectImageWithoutCode() {
        BufferedImage blank = onCanvas(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), 200, 200, 0, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> decoder.decode(blank));

        assertEquals(Messages.get("error.decode.notfound"), ex.getMessage());
    }

    @Test
    void shouldRejectFileThatIsNoImage(@TempDir Path dir) throws IOException {
        Path file = Files.writeString(dir.resolve("not-an-image.png"), "hello");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> decoder.decode(file));

        assertEquals(Messages.get("error.decode.image"), ex.getMessage());
    }

    @Test
    void shouldRejectMissingFile(@TempDir Path dir) {
        Path file = dir.resolve("missing.png");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> decoder.decode(file));

        assertEquals(Messages.get("error.decode.missing", file), ex.getMessage());
    }

    @Test
    void shouldRejectDirectory(@TempDir Path dir) {
        assertThrows(IllegalArgumentException.class, () -> decoder.decode(dir));
    }

    @Test
    void shouldRejectNullImage() {
        assertThrows(IllegalArgumentException.class, () -> decoder.decode((BufferedImage) null));
    }

    private static BufferedImage onCanvas(BufferedImage code, int width, int height, int x, int y) {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.drawImage(code, x, y, null);
        g.dispose();
        return canvas;
    }
}
