package de.zettsystems.qrcreator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Kleine Swing-Oberfläche: Text/URL eingeben, QR-Code erzeugen, als PNG speichern.
 */
final class QrCreatorApp {
    private static final int PREVIEW_SIZE = 300;
    private static final int EXPORT_SIZE = 1024;

    private final QrCodeGenerator generator = new QrCodeGenerator();
    private final JFrame frame = new JFrame("QR-Creator");
    private final JTextField inputField = new JTextField(QrCreatorMain.DEFAULT_INPUT, 30);
    private final JLabel preview = new JLabel("", SwingConstants.CENTER);
    private final JButton saveButton = new JButton("Als PNG speichern …");
    private String currentInput;

    private QrCreatorApp() {
    }

    static void start() {
        SwingUtilities.invokeLater(() -> new QrCreatorApp().show());
    }

    private void show() {
        JButton generateButton = new JButton("QR-Code erzeugen");
        generateButton.addActionListener(e -> generate());
        inputField.addActionListener(e -> generate());
        saveButton.addActionListener(e -> save());
        saveButton.setEnabled(false);

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.add(new JLabel("URL / Text:"), BorderLayout.WEST);
        top.add(inputField, BorderLayout.CENTER);
        top.add(generateButton, BorderLayout.EAST);

        preview.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(saveButton);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(top, BorderLayout.NORTH);
        content.add(preview, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        generate();
    }

    private void generate() {
        String input = inputField.getText();
        try {
            BufferedImage image = generator.generate(input, PREVIEW_SIZE, PREVIEW_SIZE);
            preview.setIcon(new ImageIcon(image));
            currentInput = input;
            saveButton.setEnabled(true);
        } catch (RuntimeException ex) {
            preview.setIcon(null);
            currentInput = null;
            saveButton.setEnabled(false);
            error(ex.getMessage());
        }
    }

    private void save() {
        if (currentInput == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PNG-Bild", "png"));
        chooser.setSelectedFile(new java.io.File("qr-code.png"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path target = chooser.getSelectedFile().toPath();
        if (!target.getFileName().toString().toLowerCase().endsWith(".png")) {
            target = target.resolveSibling(target.getFileName() + ".png");
        }
        try {
            generator.writeToFile(currentInput, EXPORT_SIZE, EXPORT_SIZE, target);
            JOptionPane.showMessageDialog(frame, "Gespeichert: " + target, "QR-Creator",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            error(ex.getMessage());
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(frame, message, "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}
