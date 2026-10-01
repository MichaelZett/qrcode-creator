package de.zettsystems.qrcreator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class QrCodeGenerator {

    public BufferedImage generate(String input, int width, int height) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(Messages.get("error.input.blank"));
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(Messages.get("error.size"));
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(input, BarcodeFormat.QR_CODE, width, height, hints(input));
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new IllegalStateException(Messages.get("error.encode"), e);
        }
    }

    /**
     * Without a hint ZXing encodes ISO-8859-1 and turns everything else (e.g. the euro sign) into "?". UTF-8 needs
     * an ECI marker that some older scanners ignore, so it is only used when ISO-8859-1 is not enough.
     */
    private static Map<EncodeHintType, Object> hints(String input) {
        if (StandardCharsets.ISO_8859_1.newEncoder().canEncode(input)) {
            return Map.of();
        }
        return Map.of(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
    }

    public void writeToFile(String input, int width, int height, Path outputFile) {
        writeImage(generate(input, width, height), outputFile);
    }

    public void writeImage(BufferedImage image, Path outputFile) {
        // ImageIO deletes the target before writing, which would silently replace an empty directory
        if (Files.isDirectory(outputFile)) {
            throw new IllegalArgumentException(Messages.get("error.directory", outputFile));
        }
        try {
            Path parent = outputFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!ImageIO.write(image, "PNG", outputFile.toFile())) {
                throw new IllegalStateException(Messages.get("error.nowriter"));
            }
        } catch (IOException e) {
            throw new IllegalStateException(Messages.get("error.write"), e);
        }
    }
}
