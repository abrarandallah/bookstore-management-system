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

    @ModelAttribute("showLanguagePopup")
    public boolean showLanguagePopup(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return true;
        }
        for (Cookie cookie : request.getCookies()) {
            if (AppLocaleResolver.COOKIE_NAME.equals(cookie.getName())) {
                return false;
            }
        }
        return true;
    }
}