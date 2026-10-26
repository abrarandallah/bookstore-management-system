package com.abrar.BOOKSTORE.controller.advice;

import com.abrar.BOOKSTORE.config.AppLocaleResolver;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Adds "showLanguagePopup" to every page's model automatically, so the
// first-visit language popup (rendered in fragments/layout.html's
// "scripts" fragment - see that file's comment for why it lives there,
// and language-popup.js for the popup's behavior) can decide whether to
// render without every individual controller needing to remember to set
// this itself.
//
// True whenever the "lang" cookie is missing - meaning nobody's ever
// explicitly picked a language (or dismissed the popup this session; see
// LanguageController#dismiss for that cookie's shorter lifetime, which is
// exactly what makes the popup return on a later visit).
@ControllerAdvice
public class LanguagePopupModelAdvice {

    // "currentPath" = where the visitor is right now, so the language links can
    // send them back to the same page after switching. Templates can't read the
    // request themselves (Thymeleaf 3.1 removed #httpServletRequest), and the site
    // sends "Referrer-Policy: no-referrer", so the Referer header is not an option.
    // The query string is kept only for the book list (its search/genre filters);
    // everywhere else it is dropped, because it can hold one-time tokens
    // (password reset, email verification) that must not be copied into another URL.
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        if (path.isEmpty() || path.startsWith("/error")) {
            return "/";
        }
        if ("/available_books".equals(path) && request.getQueryString() != null) {
            return path + "?" + request.getQueryString();
        }
        return path;
    }

    @ModelAttribute("showLanguagePopup")
    public boolean showLanguagePopup(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return true;
        }
        for (Cookie cookie : cookies) {
            if (AppLocaleResolver.COOKIE_NAME.equals(cookie.getName())) {
                return false;
            }
        }
        return true;
    }
}