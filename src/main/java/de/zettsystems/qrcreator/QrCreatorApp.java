package de.zettsystems.qrcreator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Small Swing UI (German/English): pick a type via radio button, enter the data, create the QR code, save as PNG.
 * It also reads QR codes from an image file, the clipboard or an image dropped onto the window and shows their text.
 */
final class QrCreatorApp {
    private static final int PREVIEW_SIZE = 300;
    private static final int EXPORT_SIZE = 1024;
    private static final String[] IMAGE_SUFFIXES = {"png", "jpg", "jpeg", "gif", "bmp", "tif", "tiff"};

    private final QrCodeGenerator generator = new QrCodeGenerator();
    private final QrCodeDecoder decoder = new QrCodeDecoder();
    private final JFrame frame = new JFrame();
    private final JLabel preview = new JLabel("", SwingConstants.CENTER);
    private final JButton generateButton = new JButton();
    private final JButton saveButton = new JButton();
    private final JButton decodeFileButton = new JButton();
    private final JButton decodeClipboardButton = new JButton();
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
        content.add(decodePanel(), BorderLayout.SOUTH);

        relabelActions.add(() -> frame.setTitle(Messages.get("app.title")));
        relabelActions.add(() -> generateButton.setText(Messages.get("button.generate")));
        relabelActions.add(() -> saveButton.setText(Messages.get("button.save")));
        relabelActions.add(() -> decodeFileButton.setText(Messages.get("button.decodeFile")));
        relabelActions.add(() -> decodeClipboardButton.setText(Messages.get("button.decodeClipboard")));
        relabel();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTransferHandler(new ImageDropHandler());
        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel decodePanel() {
        decodeFileButton.addActionListener(e -> decodeFile());
        decodeClipboardButton.addActionListener(e -> decodeClipboard());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(label("label.decode"));
        buttons.add(decodeFileButton);
        buttons.add(decodeClipboardButton);
        buttons.add(label("hint.drop"));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JSeparator(), BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.CENTER);
        return panel;
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

    private void decodeFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(Messages.get("filter.images"), IMAGE_SUFFIXES));
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = chooser.getSelectedFile().toPath();
        decodeInBackground(() -> decoder.decode(file));
    }

    private void decodeClipboard() {
        try {
            if (!decode(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null))) {
                error(Messages.get("error.decode.clipboard"));
            }
        } catch (UnsupportedFlavorException | IOException | IllegalStateException ex) {
            error(Messages.get("error.decode.clipboard"));
        }
    }

    /**
     * Decodes a file list (files copied in the file manager or dropped) or an image (e.g. a screenshot).
     *
     * @return false if the transferable holds neither
     */
    private boolean decode(Transferable transferable) throws UnsupportedFlavorException, IOException {
        if (transferable == null) {
            return false;
        }
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            List<?> files = (List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            if (!files.isEmpty() && files.getFirst() instanceof File file) {
                decodeInBackground(() -> decoder.decode(file.toPath()));
                return true;
            }
        }
        if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            Image image = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            decodeInBackground(() -> decoder.decode(toBufferedImage(image)));
            return true;
        }
        return false;
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage buffered) {
            return buffered;
        }
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(Messages.get("error.decode.image"));
        }
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copy;
    }

    /** Large photos take a moment with TRY_HARDER, so decoding runs off the event thread. */
    private void decodeInBackground(Supplier<List<String>> task) {
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return task.get();
            }

            @Override
            protected void done() {
                frame.setCursor(null);
                try {
                    showDecoded(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    error(Objects.requireNonNullElse(cause.getMessage(), cause.toString()));
                }
            }
        }.execute();
    }

    private void showDecoded(List<String> texts) {
        String text = String.join("\n\n", texts);
        JTextArea area = new JTextArea(text, 6, 40);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        String title = texts.size() == 1
                ? Messages.get("dialog.decoded")
                : Messages.get("dialog.decodedMany", texts.size());
        Object[] options = {Messages.get("button.copy"), Messages.get("button.close")};
        int choice = JOptionPane.showOptionDialog(frame, new JScrollPane(area), title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
        if (choice == 0) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(frame, message, Messages.get("dialog.error"), JOptionPane.ERROR_MESSAGE);
    }

    /** Accepts image files and images dropped anywhere on the window (text fields keep their own text drop). */
    private final class ImageDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || support.isDataFlavorSupported(DataFlavor.imageFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                return decode(support.getTransferable());
            } catch (UnsupportedFlavorException | IOException ex) {
                error(Messages.get("error.decode.image"));
                return false;
            }
        }
    }
}
