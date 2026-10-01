package de.zettsystems.qrcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.file.Path;

public final class QrCreatorMain {
    static final String DEFAULT_INPUT = "https://www.tg-heimfeld.com/";
    static final String DEFAULT_OUTPUT_FILE = "files/tgh.png";
    static final String DECODE_OPTION = "--decode";
    private static final Logger LOG = LoggerFactory.getLogger(QrCreatorMain.class);

    private QrCreatorMain() {
    }

    static void main(String[] args) {
        if (args == null || args.length == 0) {
            QrCreatorApp.start();
            return;
        }
        if (DECODE_OPTION.equals(args[0])) {
            // the decoded text is the program output and must be pipeable, so it goes to stdout, not the log
            decode(args, System.out); // NOSONAR
            return;
        }
        String input = resolveInput(args);
        Path outputFile = resolveOutputFile(args);

        QrCodeGenerator generator = new QrCodeGenerator();
        generator.writeToFile(input, 300, 300, outputFile);

        LOG.info("QR code written to: {}", outputFile.toAbsolutePath());
        LOG.info("Encoded text: {}", input);
    }

    static void decode(String[] args, PrintStream out) {
        if (args.length < 2 || args[1] == null || args[1].isBlank()) {
            throw new IllegalArgumentException(Messages.get("error.decode.usage"));
        }
        new QrCodeDecoder().decode(Path.of(args[1])).forEach(out::println);
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
