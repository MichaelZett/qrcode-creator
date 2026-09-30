package de.zettsystems.qrcreator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
            BitMatrix matrix = writer.encode(input, BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new IllegalStateException(Messages.get("error.encode"), e);
        }
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
