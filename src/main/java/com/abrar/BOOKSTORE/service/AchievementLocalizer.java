package com.abrar.BOOKSTORE.service;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;

// Achievements have no translation table of their own, same reasoning as
// GenreLocalizer: there are only 5 of them, defined as fixed code
// constants in AchievementService and seeded once at startup (see
// AchievementSeeder) - a full parallel entity/table felt like overkill.
// Keyed off the achievement's stable CODE_* value rather than its display
// name, since the name is just seed data and could be edited without
// breaking this mapping (the seeder itself is resilient to that - see its
// own comment).
//
// Callable directly from any template via Thymeleaf's bean-access syntax:
// ${@achievementLocalizer.localizeName(ua.achievement.code, #locale.language)}
// ${@achievementLocalizer.localizeDescription(ua.achievement.code, #locale.language)}
//
// Falls back to the raw (English) value if a code has no matching key yet
// (e.g. a new achievement added without updating messages*.properties),
// same "partial coverage is fine" philosophy as book/genre translations.
@Component
public class AchievementLocalizer {

    private final MessageSource messageSource;

    public AchievementLocalizer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String localizeName(String code, String language, String fallback) {
        return resolve(code, "name", language, fallback);
    }

    public String localizeDescription(String code, String language, String fallback) {
        return resolve(code, "description", language, fallback);
    }

    private String resolve(String code, String field, String language, String fallback) {
        if (code == null) {
            return fallback;
        }
        String key = "achievement." + code.toLowerCase(Locale.ROOT) + "." + field;
        Locale locale = Locale.forLanguageTag(language == null ? "en" : language);
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException e) {
            return fallback;
        }
    }
}