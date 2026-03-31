package de.zettsystems.qrcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class QrCreatorMain {
    static final String DEFAULT_INPUT = "https://www.tennis.de/";
    static final String DEFAULT_OUTPUT_FILE = "files/tennis.png";
    private static final Logger LOG = LoggerFactory.getLogger(QrCreatorMain.class);

    private QrCreatorMain() {
    }

    static void main(String[] args) {
        String input = resolveInput(args);
        Path outputFile = resolveOutputFile(args);

        QrCodeGenerator generator = new QrCodeGenerator();
        generator.writeToFile(input, 300, 300, outputFile);

        LOG.info("QR code written to: {}", outputFile.toAbsolutePath());
        LOG.info("Encoded text: {}", input);
    }

    static String resolveInput(String[] args) {
        if (args == null || args.length == 0 || args[0] == null || args[0].isBlank()) {
            return DEFAULT_INPUT;
        }
        return args[0];
    }

    static Path resolveOutputFile(String[] args) {
        if (args == null || args.length < 2 || args[1] == null || args[1].isBlank()) {
            return Path.of(DEFAULT_OUTPUT_FILE);
        }
        return Path.of(args[1]);
    }
}
