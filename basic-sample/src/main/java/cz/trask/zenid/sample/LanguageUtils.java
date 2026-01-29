package cz.trask.zenid.sample;

import java.util.Locale;

import cz.trask.zenid.sdk.enums.SupportedLanguages;

public class LanguageUtils {

    public static SupportedLanguages getLanguage() {
        Locale locale = Locale.getDefault();
        String languageName = locale.getDisplayLanguage(Locale.ENGLISH);
        String languageCode = locale.getLanguage();

        try {
            return SupportedLanguages.valueOf(languageName);
        } catch (IllegalArgumentException e) {
            switch (languageCode) {
                case "cs": return SupportedLanguages.Czech;
                case "pl": return SupportedLanguages.Polish;
                case "de": return SupportedLanguages.German;
                case "hr": return SupportedLanguages.Croatian;
                case "sk": return SupportedLanguages.Slovak;
                case "nl": return SupportedLanguages.Dutch;
                default: return SupportedLanguages.English;
            }
        }
    }
}
