package de.zettsystems.qrcreator;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * User-facing texts in German and English. The language is chosen once at startup from the system
 * locale (German if the system language is German, English otherwise) and can be switched in the UI.
 */
final class Messages {
    static final List<Locale> SUPPORTED = List.of(Locale.GERMAN, Locale.ENGLISH);

    private static final String BUNDLE = "messages";
    private static Locale locale = fromSystem(Locale.getDefault());

    private Messages() {
    }

    static Locale fromSystem(Locale system) {
        return Locale.GERMAN.getLanguage().equals(system.getLanguage()) ? Locale.GERMAN : Locale.ENGLISH;
    }

    static Locale locale() {
        return locale;
    }

    static void setLocale(Locale newLocale) {
        locale = fromSystem(newLocale);
    }

    static String get(String key, Object... args) {
        String pattern = ResourceBundle.getBundle(BUNDLE, locale).getString(key);
        return args.length == 0 ? pattern : MessageFormat.format(pattern, args);
    }
}
