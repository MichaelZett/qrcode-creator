package de.zettsystems.qrcreator;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Builds the payload for common QR code types.
 */
final class QrPayload {

    private QrPayload() {
    }

    static String text(String text) {
        return requireNotBlank(text, "field.text");
    }

    static String mailto(String address, String subject, String body) {
        StringBuilder sb = new StringBuilder("mailto:").append(requireNotBlank(address, "field.mail").trim());
        String separator = "?";
        if (subject != null && !subject.isBlank()) {
            sb.append(separator).append("subject=").append(encode(subject));
            separator = "&";
        }
        if (body != null && !body.isBlank()) {
            sb.append(separator).append("body=").append(encode(body));
        }
        return sb.toString();
    }

    static String tel(String number) {
        return "tel:" + requireNotBlank(number, "field.phone").replaceAll("[\\s()/-]", "");
    }

    /**
     * @param encryption "WPA", "WEP" or "nopass"
     */
    static String wifi(String ssid, String password, String encryption) {
        requireNotBlank(ssid, "field.ssid");
        StringBuilder sb = new StringBuilder("WIFI:T:").append(encryption).append(";S:").append(escapeWifi(ssid)).append(';');
        if (!"nopass".equals(encryption)) {
            sb.append("P:").append(escapeWifi(requireNotBlank(password, "field.password"))).append(';');
        }
        return sb.append(';').toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String escapeWifi(String value) {
        return value.replaceAll("([\\\\;,:\"])", "\\\\$1");
    }

    private static String requireNotBlank(String value, String labelKey) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(Messages.get("error.blank", Messages.get(labelKey)));
        }
        return value;
    }
}
