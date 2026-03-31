package de.zettsystems.qrcreator;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeGeneratorTest {

    private final QrCodeGenerator generator = new QrCodeGenerator();

    @Test
    void shouldGenerateQrImageForValidInput() {
        BufferedImage image = generator.generate("https://zett.systems", 256, 256);

        assertNotNull(image);
        assertEquals(256, image.getWidth());
        assertEquals(256, image.getHeight());
    }

    @Test
    void shouldRejectBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(" ", 128, 128));
    }

    @Test
    void shouldRejectNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate("abc", 0, 128));
        assertThrows(IllegalArgumentException.class, () -> generator.generate("abc", 128, -1));
    }
}
