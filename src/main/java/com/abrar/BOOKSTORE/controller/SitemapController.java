package com.abrar.BOOKSTORE.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Serves /robots.txt and /sitemap.xml. Both are generated (not static
 * files) because robots.txt's "Sitemap:" line needs an absolute URL per
 * spec, and a static file can't know the real deployment domain -
 * hardcoding localhost:8081 into either would be silently wrong on any
 * real deployment. See app.base-url (README's Configuration table).
 */
@Controller
public class SitemapController {

    // Deliberately limited to the app's fixed pages, not the book catalog
    // itself - the catalog is database-driven and would need real
    // generation logic against the Book repository to stay accurate as
    // books are added or removed, rather than a handful of hardcoded
    // entries here. /available_books is included and search engines can
    // still reach individual books by following links from there - they
    // just won't get a dedicated <url> entry per book.
    private static final List<String> PUBLIC_PAGES = List.of(
            "/", "/about", "/available_books", "/genres", "/privacy", "/terms");

    // Everything reachable without logging in (see SecurityConfig) but
    // NOT worth a search engine crawling - form-action/API routes, or
    // pages that only make sense mid-flow (e.g. /forgot-password).
    private static final List<String> DISALLOWED_PREFIXES = List.of(
            "/admin/", "/my_books", "/reading-history", "/profile", "/settings",
            "/book_register", "/api/", "/available_books/results",
            "/login", "/register", "/forgot-password", "/reset-password",
            "/verify-email", "/resend-verification", "/change-password", "/delete-account");

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String page : PUBLIC_PAGES) {
            xml.append("  <url><loc>").append(baseUrl).append(page).append("</loc></url>\n");
        }
        xml.append("</urlset>\n");
        return xml.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        StringBuilder txt = new StringBuilder();
        txt.append("User-agent: *\n");
        txt.append("Sitemap: ").append(baseUrl).append("/sitemap.xml\n\n");
        for (String page : PUBLIC_PAGES) {
            txt.append("Allow: ").append(page).append("\n");
        }
        txt.append("\n");
        for (String prefix : DISALLOWED_PREFIXES) {
            txt.append("Disallow: ").append(prefix).append("\n");
        }
        return txt.toString();
    }
}