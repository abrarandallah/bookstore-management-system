package com.abrar.BOOKSTORE.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

// Every page template (see fragments/layout.html and the ~28 page files
// that each declare their own <html> tag) was written with a static
// <html lang="en" ...> or no lang attribute at all - there's no single
// shared place in the Thymeleaf template structure where all of them
// resolve to one fragment, since each page owns its own <html> root.
//
// Rather than hand-editing all ~28 templates to add
// th:lang/th:dir="${#locale...}" (real churn, and every future new page
// would need to remember to include it too), this filter rewrites the
// opening <html ...> tag on every HTML response after Thymeleaf has
// rendered it, based on whatever AppLocaleResolver resolved for this
// request. One place, applies automatically to every current and future
// page, no template changes needed.
//
// Only runs on responses that are actually HTML - CSS/JS/images/uploads
// and the JSON DTO endpoint (BookController#getBookById) are left alone.
@Component
public class HtmlLangDirFilter extends OncePerRequestFilter {

    private final LocaleResolver localeResolver;

    public HtmlLangDirFilter(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, wrapper);

        String contentType = wrapper.getContentType();
        if (contentType == null || !contentType.startsWith("text/html")) {
            wrapper.copyBodyToResponse();
            return;
        }

        byte[] body = wrapper.getContentAsByteArray();
        String html = new String(body, StandardCharsets.UTF_8);

        Locale locale = localeResolver.resolveLocale(request);
        String lang = locale.getLanguage();
        String dir = "ar".equals(lang) ? "rtl" : "ltr";
        String replacementAttrs = "<html lang=\"" + lang + "\" dir=\"" + dir + "\"";

        // Every page's opening tag starts with "<html" followed by either
        // nothing, lang="en", or the xmlns:* declarations - all of those
        // are matched by this, and the xmlns:* part (needed for th:*/sec:*
        // attributes to keep validating) is preserved untouched since it's
        // outside the captured "<html[ lang=\"en\"]" prefix being replaced.
        String rewritten = html.replaceFirst("<html(\\s+lang=\"[^\"]*\")?", replacementAttrs);

        byte[] rewrittenBytes = rewritten.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(rewrittenBytes.length);
        response.getOutputStream().write(rewrittenBytes);
    }
}