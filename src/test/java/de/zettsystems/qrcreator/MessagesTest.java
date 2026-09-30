package de.zettsystems.qrcreator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest {

    private final Locale original = Messages.locale();

    @AfterEach
    void restoreLocale() {
        Messages.setLocale(original);
    }

    @Test
    void shouldMapGermanSystemLanguageToGermanAndEverythingElseToEnglish() {
        assertEquals(Locale.GERMAN, Messages.fromSystem(Locale.GERMANY));
        assertEquals(Locale.GERMAN, Messages.fromSystem(Locale.of("de", "AT")));
        assertEquals(Locale.ENGLISH, Messages.fromSystem(Locale.FRANCE));
        assertEquals(Locale.ENGLISH, Messages.fromSystem(Locale.US));
    }

    @Test
    void shouldSwitchLanguage() {
        Messages.setLocale(Locale.ENGLISH);
        String english = Messages.get("button.generate");
        Messages.setLocale(Locale.GERMAN);

        assertNotEquals(english, Messages.get("button.generate"));
    }

    @Test
    void shouldFormatArguments() {
        Messages.setLocale(Locale.ENGLISH);

        assertEquals("Text must not be empty", Messages.get("error.blank", Messages.get("field.text")));
    }

    @Test
    void shouldFailForUnknownKey() {
        assertThrows(MissingResourceException.class, () -> Messages.get("does.not.exist"));
    }

    @Test
    void shouldProvideEveryKeyInBothLanguages() {
        ResourceBundle english = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        ResourceBundle german = ResourceBundle.getBundle("messages", Locale.GERMAN);

        assertEquals(english.keySet(), german.keySet());
        assertTrue(english.keySet().stream().noneMatch(k -> english.getString(k).isBlank()));
    }
}
