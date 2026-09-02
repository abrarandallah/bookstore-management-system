package com.abrar.BOOKSTORE.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Set;

// Resolves the visitor's chosen language from a single cookie ("lang"),
// rather than Spring's built-in CookieLocaleResolver - that class uses one
// fixed cookie lifetime for every change, and the language-picker design
// needs two different lifetimes depending on how a language was chosen:
//   - Explicitly picking a language (popup buttons or Settings) needs a
//     long-lived cookie, so the choice sticks and the first-visit popup
//     never asks again on that browser. That's what setLocale() below does,
//     called from LanguageController's /language endpoint.
//   - Dismissing the first-visit popup without picking needs a
//     *session-only* cookie (so it asks again on a later visit, but not on
//     every single page during the visit just dismissed) - that's set
//     directly by LanguageController's /language/dismiss endpoint, which
//     deliberately bypasses setLocale() here to get that shorter lifetime.
//     See that controller for the actual cookie-writing code for that case.
//
// Registered as the bean named "localeResolver" - that specific name is
// what Spring's DispatcherServlet looks for, and is also what makes
// Thymeleaf's #locale (used for th:lang/th:dir - see HtmlLangDirFilter, and
// eventually #{...} message-bundle lookups in Phase 2) resolve through this
// class automatically.
@Component("localeResolver")
public class AppLocaleResolver implements LocaleResolver {

    public static final String COOKIE_NAME = "lang";
    private static final int PERMANENT_MAX_AGE_SECONDS = 60 * 60 * 24 * 365; // ~1 year
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "fr", "ar");
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return DEFAULT_LOCALE;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName()) && SUPPORTED_LANGUAGES.contains(cookie.getValue())) {
                return Locale.forLanguageTag(cookie.getValue());
            }
        }
        return DEFAULT_LOCALE;
    }

    // Called by LanguageController's /language endpoint for an explicit
    // pick - always writes a permanent cookie. Session-only writes (the
    // "dismissed the popup" case) intentionally don't go through here; see
    // the class comment above.
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        String language = (locale == null || !SUPPORTED_LANGUAGES.contains(locale.getLanguage()))
                ? DEFAULT_LOCALE.getLanguage()
                : locale.getLanguage();
        Cookie cookie = new Cookie(COOKIE_NAME, language);
        cookie.setPath("/");
        cookie.setMaxAge(PERMANENT_MAX_AGE_SECONDS);
        response.addCookie(cookie);
    }
}