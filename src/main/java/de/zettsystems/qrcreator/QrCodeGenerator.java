package de.zettsystems.qrcreator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QrCodeGenerator {

    public BufferedImage generate(String input, int width, int height) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("input darf nicht leer sein");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width und height müssen > 0 sein");
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(input, BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new IllegalStateException("QR-Code konnte nicht erzeugt werden", e);
        }
    }

    public void writeToFile(String input, int width, int height, Path outputFile) {
        try {
            if (input == null || input.isBlank()) {
                throw new IllegalArgumentException("input darf nicht leer sein");
            }
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("width und height müssen > 0 sein");
            }

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(input, BarcodeFormat.QR_CODE, width, height);
            Path parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            MatrixToImageWriter.writeToPath(matrix, "PNG", outputFile);
        } catch (WriterException e) {
            throw new IllegalStateException("QR-Code konnte nicht erzeugt werden", e);
        } catch (IOException e) {
            throw new IllegalStateException("QR-Code konnte nicht geschrieben werden", e);
        }
    }
}
