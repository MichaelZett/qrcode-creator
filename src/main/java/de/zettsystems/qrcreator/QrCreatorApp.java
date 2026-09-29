package de.zettsystems.qrcreator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Kleine Swing-Oberfläche: Typ per RadioButton wählen, Daten eingeben, QR-Code erzeugen, als PNG speichern.
 */
final class QrCreatorApp {
    private static final int PREVIEW_SIZE = 300;
    private static final int EXPORT_SIZE = 1024;

    private final QrCodeGenerator generator = new QrCodeGenerator();
    private final JFrame frame = new JFrame("QR-Creator");
    private final JLabel preview = new JLabel("", SwingConstants.CENTER);
    private final JButton saveButton = new JButton("Als PNG speichern …");
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private Supplier<String> payloadSupplier;
    private String currentPayload;

    private final JTextField textField = new JTextField(QrCreatorMain.DEFAULT_INPUT, 28);
    private final JTextField mailAddress = new JTextField(28);
    private final JTextField mailSubject = new JTextField(28);
    private final JTextArea mailBody = new JTextArea(3, 28);
    private final JTextField phone = new JTextField(28);
    private final JTextField wifiSsid = new JTextField(28);
    private final JPasswordField wifiPassword = new JPasswordField(28);
    private final JComboBox<String> wifiEncryption = new JComboBox<>(new String[]{"WPA", "WEP", "nopass"});

    private QrCreatorApp() {
    }

    static void start() {
        SwingUtilities.invokeLater(() -> new QrCreatorApp().show());
    }

    private void show() {
        ButtonGroup group = new ButtonGroup();
        JPanel types = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addType(types, group, "URL / Text", "text", () -> QrPayload.text(textField.getText()), textPanel(), true);
        addType(types, group, "E-Mail (mailto:)", "mail",
                () -> QrPayload.mailto(mailAddress.getText(), mailSubject.getText(), mailBody.getText()), mailPanel(), false);
        addType(types, group, "Telefon (tel:)", "tel", () -> QrPayload.tel(phone.getText()), phonePanel(), false);
        addType(types, group, "WLAN", "wifi",
                () -> QrPayload.wifi(wifiSsid.getText(), new String(wifiPassword.getPassword()), (String) wifiEncryption.getSelectedItem()),
                wifiPanel(), false);

        JButton generateButton = new JButton("QR-Code erzeugen");
        generateButton.addActionListener(e -> generate());
        saveButton.addActionListener(e -> save());
        saveButton.setEnabled(false);
        preview.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(generateButton);
        buttons.add(saveButton);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.add(types, BorderLayout.NORTH);
        top.add(cardPanel, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(top, BorderLayout.NORTH);
        content.add(preview, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        generate();
    }

    private void addType(JPanel typePanel, ButtonGroup group, String label, String card,
                         Supplier<String> supplier, JPanel form, boolean selected) {
        JRadioButton radio = new JRadioButton(label, selected);
        radio.addActionListener(e -> select(card, supplier));
        group.add(radio);
        typePanel.add(radio);
        cardPanel.add(form, card);
        if (selected) {
            select(card, supplier);
        }
    }

    private void select(String card, Supplier<String> supplier) {
        payloadSupplier = supplier;
        cards.show(cardPanel, card);
        frame.pack();
    }

    private JPanel textPanel() {
        textField.addActionListener(e -> generate());
        return form(new String[]{"URL / Text:"}, new JComponent[]{textField});
    }

    private JPanel mailPanel() {
        return form(new String[]{"An:", "Betreff:", "Text:"},
                new JComponent[]{mailAddress, mailSubject, new JScrollPane(mailBody)});
    }

    private JPanel phonePanel() {
        return form(new String[]{"Nummer:"}, new JComponent[]{phone});
    }

    private JPanel wifiPanel() {
        return form(new String[]{"Netzwerk (SSID):", "Passwort:", "Verschlüsselung:"},
                new JComponent[]{wifiSsid, wifiPassword, wifiEncryption});
    }

    private static JPanel form(String[] labels, JComponent[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.NORTHWEST;
        for (int i = 0; i < labels.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            panel.add(new JLabel(labels[i]), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(fields[i], c);
        }
        return panel;
    }

    private void generate() {
        try {
            String payload = payloadSupplier.get();
            BufferedImage image = generator.generate(payload, PREVIEW_SIZE, PREVIEW_SIZE);
            preview.setIcon(new ImageIcon(image));
            currentPayload = payload;
            saveButton.setEnabled(true);
        } catch (RuntimeException ex) {
            preview.setIcon(null);
            currentPayload = null;
            saveButton.setEnabled(false);
            error(ex.getMessage());
        }
    }

    private void save() {
        if (currentPayload == null) {
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
            generator.writeToFile(currentPayload, EXPORT_SIZE, EXPORT_SIZE, target);
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
