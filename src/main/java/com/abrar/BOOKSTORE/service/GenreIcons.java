package com.abrar.BOOKSTORE.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

// Picks a Font Awesome icon for a genre from keywords in its English name, so
// the book list can show a recognisable symbol on every book and genre chip
// without a database column. Genres added later that match no keyword simply
// get the open-book icon.
//
// Used from templates: ${@genreIcons.iconFor(g.name)}
@Component
public class GenreIcons {

    static final String DEFAULT_ICON = "fa-book-open";

    // Order matters: the first keyword found in the name wins, so the more
    // specific ones ("psychological horror") come before the general ones.
    private static final Map<String, String> KEYWORDS = new LinkedHashMap<>();
    static {
        KEYWORDS.put("psychological", "fa-brain");
        KEYWORDS.put("horror", "fa-ghost");
        KEYWORDS.put("gothic", "fa-skull");
        KEYWORDS.put("supernatural", "fa-ghost");
        KEYWORDS.put("legal", "fa-scale-balanced");
        KEYWORDS.put("crime", "fa-user-secret");
        KEYWORDS.put("heist", "fa-user-secret");
        KEYWORDS.put("mystery", "fa-magnifying-glass");
        KEYWORDS.put("thriller", "fa-bolt");
        KEYWORDS.put("suspense", "fa-bolt");
        KEYWORDS.put("science fiction", "fa-rocket");
        KEYWORDS.put("speculative", "fa-rocket");
        KEYWORDS.put("steampunk", "fa-gear");
        KEYWORDS.put("dystopian", "fa-city");
        KEYWORDS.put("fantasy", "fa-dragon");
        KEYWORDS.put("magical", "fa-wand-magic-sparkles");
        KEYWORDS.put("mythology", "fa-landmark");
        KEYWORDS.put("historical", "fa-landmark");
        KEYWORDS.put("archaeology", "fa-hourglass-half");
        KEYWORDS.put("war", "fa-shield-halved");
        KEYWORDS.put("political", "fa-flag");
        KEYWORDS.put("adventure", "fa-compass");
        KEYWORDS.put("exploration", "fa-compass");
        KEYWORDS.put("travel", "fa-earth-europe");
        KEYWORDS.put("survival", "fa-fire");
        KEYWORDS.put("romance", "fa-heart");
        KEYWORDS.put("relationship", "fa-heart");
        KEYWORDS.put("friendship", "fa-handshake");
        KEYWORDS.put("human connection", "fa-people-arrows");
        KEYWORDS.put("community", "fa-people-group");
        KEYWORDS.put("comedy", "fa-face-laugh-beam");
        KEYWORDS.put("satire", "fa-face-laugh-beam");
        KEYWORDS.put("drama", "fa-masks-theater");
        KEYWORDS.put("grief", "fa-cloud-rain");
        KEYWORDS.put("slice of life", "fa-mug-hot");
        KEYWORDS.put("coming of age", "fa-seedling");
        KEYWORDS.put("young adult", "fa-seedling");
        KEYWORDS.put("animal", "fa-paw");
        KEYWORDS.put("environmental", "fa-leaf");
        KEYWORDS.put("music", "fa-music");
        KEYWORDS.put("art", "fa-palette");
        KEYWORDS.put("linguistic", "fa-language");
        KEYWORDS.put("communication", "fa-comments");
        KEYWORDS.put("artificial intelligence", "fa-robot");
        KEYWORDS.put("technology", "fa-microchip");
        KEYWORDS.put("business", "fa-briefcase");
        KEYWORDS.put("career", "fa-briefcase");
        KEYWORDS.put("workplace", "fa-building");
        KEYWORDS.put("productivity", "fa-list-check");
        KEYWORDS.put("personal development", "fa-arrow-trend-up");
        KEYWORDS.put("self-growth", "fa-arrow-trend-up");
        KEYWORDS.put("mental", "fa-spa");
        KEYWORDS.put("psychology", "fa-brain");
        KEYWORDS.put("philosophy", "fa-lightbulb");
        KEYWORDS.put("memoir", "fa-feather-pointed");
        KEYWORDS.put("lifestyle", "fa-mug-hot");
        KEYWORDS.put("literary", "fa-feather-pointed");
        KEYWORDS.put("contemporary", "fa-feather-pointed");
    }

    public String iconFor(String genreName) {
        if (genreName == null) {
            return DEFAULT_ICON;
        }
        String n = genreName.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> e : KEYWORDS.entrySet()) {
            if (n.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return DEFAULT_ICON;
    }
}
