package de.zettsystems.qrcreator;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads the text of the QR codes in an image (PNG, JPEG, GIF, BMP, TIFF - whatever ImageIO can read).
 */
public class QrCodeDecoder {

    private static final Map<DecodeHintType, Object> HINTS = Map.of(DecodeHintType.TRY_HARDER, Boolean.TRUE);

    /**
     * @return the texts of all QR codes found, in the order found, without duplicates
     * @throws IllegalArgumentException if the image contains no readable QR code
     */
    public List<String> decode(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException(Messages.get("error.decode.image"));
        }
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        try {
            return decode(source);
        } catch (ReaderException e) {
            // light code on dark background; the ALSO_INVERTED hint is only honoured by MultiFormatReader
            try {
                return decode(source.invert());
            } catch (ReaderException inverted) {
                throw new IllegalArgumentException(Messages.get("error.decode.notfound"), e);
            }
        }
    }

    /**
     * @throws IllegalArgumentException if the file is missing, no image or contains no readable QR code
     */
    public List<String> decode(Path imageFile) {
        if (!Files.isRegularFile(imageFile)) {
            throw new IllegalArgumentException(Messages.get("error.decode.missing", imageFile));
        }
        BufferedImage image;
        try {
            image = ImageIO.read(imageFile.toFile());
        } catch (IOException e) {
            throw new IllegalArgumentException(Messages.get("error.decode.image"), e);
        }
        return decode(image);
    }

    private static List<String> decode(LuminanceSource source) throws ReaderException {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Set<String> texts = new LinkedHashSet<>();
            for (Result result : new QRCodeMultiReader().decodeMultiple(bitmap, HINTS)) {
                texts.add(result.getText());
            }
            return new ArrayList<>(texts);
        } catch (ReaderException e) {
            // the multi reader misses some single codes (e.g. in photos), the single reader is more tolerant
            return List.of(new QRCodeReader().decode(bitmap, HINTS).getText());
        }
    }
}
