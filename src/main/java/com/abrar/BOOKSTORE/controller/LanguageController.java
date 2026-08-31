package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.config.AppLocaleResolver;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Set;

// Both endpoints are GET, not POST: this only ever changes a display
// preference, never persists anything tied to an account or another user,
// so it doesn't need CSRF protection - the same reasoning the theme toggle
// (pure client-side, no server round-trip at all) and other read-only links
// in this app already follow. See SecurityConfig's permitAll list - both
// routes are listed there since guests (not just logged-in users) can
// browse and read books, and need to be able to change language too.
@Controller
public class LanguageController {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "fr", "ar");

    @Autowired
    private LocaleResolver localeResolver;

    // Explicit pick - from the first-visit popup's language buttons, its
    // "Continue in English" option, or the Settings page. Sets a permanent
    // cookie (see AppLocaleResolver.setLocale), so the popup never shows
    // again on this browser.
    @GetMapping("/language")
    public String change(@RequestParam String lang, @RequestParam(required = false) String redirect,
            HttpServletRequest request, HttpServletResponse response) {
        String safeLang = SUPPORTED_LANGUAGES.contains(lang) ? lang : "en";
        localeResolver.setLocale(request, response, Locale.forLanguageTag(safeLang));
        return "redirect:" + safeRedirect(redirect);
    }

    // Dismissing the popup (closing it, clicking outside it) without
    // picking a language. Deliberately bypasses AppLocaleResolver.setLocale
    // to write a *session-only* cookie instead of a permanent one - see
    // AppLocaleResolver's class comment for why this needs a different
    // lifetime than an explicit pick. Called quietly by JS in the
    // background (see language-popup.js) so dismissing doesn't reload the
    // page.
    @GetMapping("/language/dismiss")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dismiss(HttpServletResponse response) {
        Cookie cookie = new Cookie(AppLocaleResolver.COOKIE_NAME, "en");
        cookie.setPath("/");
        // No setMaxAge() call: a cookie with no Max-Age/Expires is a
        // session cookie, cleared when the browser closes - deliberately
        // so the popup asks again on a later visit, not never again.
        response.addCookie(cookie);
    }

    // Only allow same-site relative paths - a redirect param that pointed
    // at an external URL would make this an open redirect.
    private String safeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/") || redirect.startsWith("//")) {
            return "/";
        }
        return redirect;
    }
}