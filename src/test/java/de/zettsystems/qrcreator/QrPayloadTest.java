package de.zettsystems.qrcreator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QrPayloadTest {

    @Test
    void shouldBuildSimpleMailto() {
        assertEquals("mailto:max@example.com", QrPayload.mailto("max@example.com", "", null));
    }

    @Test
    void shouldEncodeSubjectAndBody() {
        assertEquals("mailto:max@example.com?subject=Anfrage%20neu&body=Hallo%2C%0Awelt",
                QrPayload.mailto("max@example.com", "Anfrage neu", "Hallo,\nwelt"));
    }

    @Test
    void shouldUseAmpersandOnlyBetweenParameters() {
        assertEquals("mailto:a@b.de?body=x", QrPayload.mailto("a@b.de", null, "x"));
    }

    @Test
    void shouldStripFormattingFromPhoneNumber() {
        assertEquals("tel:+491701234567", QrPayload.tel("+49 (170) 123-4567"));
    }

    @Test
    void shouldBuildWifiWithEscaping() {
        assertEquals("WIFI:T:WPA;S:Home\\;Net;P:pa\\:ss;;", QrPayload.wifi("Home;Net", "pa:ss", "WPA"));
        assertEquals("WIFI:T:nopass;S:Cafe;;", QrPayload.wifi("Cafe", "", "nopass"));
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> QrPayload.mailto(" ", null, null));
        assertThrows(IllegalArgumentException.class, () -> QrPayload.tel(""));
        assertThrows(IllegalArgumentException.class, () -> QrPayload.wifi("x", "", "WPA"));
        assertThrows(IllegalArgumentException.class, () -> QrPayload.text(null));
    }
}
