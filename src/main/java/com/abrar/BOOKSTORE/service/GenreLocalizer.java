package com.abrar.BOOKSTORE.service;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;

// Genres have no translation table of their own (unlike Book, see
// BookTranslationService) - there are only ~58 of them, added rarely
// through the admin UI, so a full parallel entity/table/service felt like
// overkill. Instead, each genre's English name is turned into a message
// key (see slugify()) and looked up in messages.properties /
// messages_ar.properties / messages_fr.properties, the same files every
// other piece of static UI text already uses.
//
// Callable directly from any template via Thymeleaf's bean-access syntax:
// ${@genreLocalizer.localizeName(g.name, #locale.language)}
//
// If a genre is added via the admin UI and nobody's added a matching
// genre.<slug> key yet, this safely falls back to the raw (English) name
// rather than showing a broken "??genre.xxx??" placeholder - the same
// "partial coverage is fine" philosophy as book translations.
@Component
public class GenreLocalizer {

    private final MessageSource messageSource;

    public GenreLocalizer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String localizeName(String genreName, String language) {
        if (genreName == null) {
            return null;
        }
        String key = "genre." + slugify(genreName);
        Locale locale = Locale.forLanguageTag(language == null ? "en" : language);
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException e) {
            return genreName;
        }
    }

    // Mirrors how these keys were generated when the initial 58 genres
    // were added: lowercase, any run of non-alphanumeric characters
    // collapsed into a single ".", leading/trailing "." trimmed. E.g.
    // "Coming of Age" -> "coming.of.age", "Self-Growth" -> "self.growth".
    private String slugify(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("^\\.+|\\.+$", "");
    }
}