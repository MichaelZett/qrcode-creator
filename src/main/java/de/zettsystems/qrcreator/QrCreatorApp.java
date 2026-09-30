package de.zettsystems.qrcreator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Small Swing UI (German/English): pick a type via radio button, enter the data, create the QR code, save as PNG.
 */
final class QrCreatorApp {
    private static final int PREVIEW_SIZE = 300;
    private static final int EXPORT_SIZE = 1024;

    private final QrCodeGenerator generator = new QrCodeGenerator();
    private final JFrame frame = new JFrame();
    private final JLabel preview = new JLabel("", SwingConstants.CENTER);
    private final JButton generateButton = new JButton();
    private final JButton saveButton = new JButton();
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final List<Runnable> relabelActions = new ArrayList<>();
    private Supplier<String> payloadSupplier;
    private String currentPayload;

    private final JTextField textField = new JTextField(28);
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
        addType(types, group, "type.text", "text", () -> QrPayload.text(textField.getText()), textPanel(), true);
        addType(types, group, "type.mail", "mail",
                () -> QrPayload.mailto(mailAddress.getText(), mailSubject.getText(), mailBody.getText()), mailPanel(), false);
        addType(types, group, "type.tel", "tel", () -> QrPayload.tel(phone.getText()), phonePanel(), false);
        addType(types, group, "type.wifi", "wifi",
                () -> QrPayload.wifi(wifiSsid.getText(), new String(wifiPassword.getPassword()), (String) wifiEncryption.getSelectedItem()),
                wifiPanel(), false);

        generateButton.addActionListener(e -> generate());
        saveButton.addActionListener(e -> save());
        saveButton.setEnabled(false);
        preview.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(generateButton);
        buttons.add(saveButton);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        JPanel header = new JPanel(new BorderLayout());
        header.add(types, BorderLayout.CENTER);
        header.add(languagePanel(), BorderLayout.EAST);
        top.add(header, BorderLayout.NORTH);
        top.add(cardPanel, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(top, BorderLayout.NORTH);
        content.add(preview, BorderLayout.CENTER);

        relabelActions.add(() -> frame.setTitle(Messages.get("app.title")));
        relabelActions.add(() -> generateButton.setText(Messages.get("button.generate")));
        relabelActions.add(() -> saveButton.setText(Messages.get("button.save")));
        relabel();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel languagePanel() {
        JComboBox<Locale> language = new JComboBox<>(Messages.SUPPORTED.toArray(new Locale[0]));
        language.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Locale locale = (Locale) value;
                return super.getListCellRendererComponent(list, locale.getDisplayLanguage(locale), index,
                        isSelected, cellHasFocus);
            }
        });
        language.setSelectedItem(Messages.locale());
        language.addActionListener(e -> {
            Messages.setLocale((Locale) language.getSelectedItem());
            relabel();
            frame.pack();
        });
        JLabel label = label("language");
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(label);
        panel.add(language);
        return panel;
    }

    private JLabel label(String key) {
        JLabel label = new JLabel();
        relabelActions.add(() -> label.setText(Messages.get(key)));
        return label;
    }

    private void relabel() {
        // dialogs and the file chooser take their standard texts from this default
        JComponent.setDefaultLocale(Messages.locale());
        relabelActions.forEach(Runnable::run);
    }

    private void addType(JPanel typePanel, ButtonGroup group, String labelKey, String card,
                         Supplier<String> supplier, JPanel form, boolean selected) {
        JRadioButton radio = new JRadioButton();
        radio.setSelected(selected);
        relabelActions.add(() -> radio.setText(Messages.get(labelKey)));
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
        return form(new String[]{"label.text"}, new JComponent[]{textField});
    }

    private JPanel mailPanel() {
        return form(new String[]{"label.mailTo", "label.mailSubject", "label.mailBody"},
                new JComponent[]{mailAddress, mailSubject, new JScrollPane(mailBody)});
    }

    private JPanel phonePanel() {
        return form(new String[]{"label.phone"}, new JComponent[]{phone});
    }

    private JPanel wifiPanel() {
        return form(new String[]{"label.ssid", "label.password", "label.encryption"},
                new JComponent[]{wifiSsid, wifiPassword, wifiEncryption});
    }

    private JPanel form(String[] labelKeys, JComponent[] fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.NORTHWEST;
        for (int i = 0; i < labelKeys.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            panel.add(label(labelKeys[i]), c);
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
        chooser.setFileFilter(new FileNameExtensionFilter(Messages.get("filter.png"), "png"));
        chooser.setSelectedFile(new java.io.File("qr-code.png"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path target = chooser.getSelectedFile().toPath();
        if (!target.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")) {
            target = target.resolveSibling(target.getFileName() + ".png");
        }
        try {
            generator.writeToFile(currentPayload, EXPORT_SIZE, EXPORT_SIZE, target);
            JOptionPane.showMessageDialog(frame, Messages.get("dialog.saved", target), Messages.get("app.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            error(ex.getMessage());
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(frame, message, Messages.get("dialog.error"), JOptionPane.ERROR_MESSAGE);
    }
}
