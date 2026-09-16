"""
One-time batch translator for the app's UI strings (buttons, labels,
menus - as opposed to translate_books.py, which handles book/page
content). Reads messages.properties (English, key=value, produced by
extract_ui_strings.py), sends each value to the same self-hosted
LibreTranslate instance already used for book translation, and writes
messages_ar.properties and messages_fr.properties - Spring Boot picks
these up automatically by filename convention, no extra config needed
beyond spring.messages.basename already being "messages" (the default).

Requires the same LibreTranslate container used for book translation,
already running on the app's Docker network:

    docker run -d --name libretranslate \
        --network bookstore-management-system_default \
        -p 5000:5000 libretranslate/libretranslate --load-only en,ar,fr

Usage (from the project root):

    docker run --rm --network bookstore-management-system_default \
        -v "${PWD}:/app" -w /app \
        python:3.12-slim \
        sh -c "pip install -q requests && python -u translate_ui_messages.py"

Environment variables:
    LIBRETRANSLATE_URL (default: http://libretranslate:5000/translate)
    TARGET_LANGUAGES   (default: ar,fr)

A small number of keys are deliberately left untranslated (copied as-is
into every language file) - see EXCLUDE_FROM_TRANSLATION below - because
they're not plain English to begin with (e.g. a display hint that
intentionally mixes Arabic and French text to preview both options in
the language picker itself).
"""
import os
import time

import requests

LIBRETRANSLATE_URL = os.environ.get("LIBRETRANSLATE_URL", "http://libretranslate:5000/translate")
TARGET_LANGUAGES = os.environ.get("TARGET_LANGUAGES", "ar,fr").split(",")

MESSAGES_FILE = "messages.properties"

# Keys whose English value should be copied through unchanged rather
# than sent to LibreTranslate - see the module docstring for why.
EXCLUDE_FROM_TRANSLATION = {
    "fragments_layout.middot.choisissez.votre.langue",
}


def translate_text(text, target_lang):
    """Same retry/timeout behavior as translate_books.py's version:
    a transient LibreTranslate failure is retried a few times, and a
    row that fails every attempt is skipped (returns None) rather than
    crashing the whole run."""
    for attempt in range(4):
        try:
            response = requests.post(
                LIBRETRANSLATE_URL,
                json={
                    "q": text,
                    "source": "en",
                    "target": target_lang,
                    "format": "text",
                },
                timeout=120,
            )
            response.raise_for_status()
            return response.json()["translatedText"]
        except Exception as e:
            print(f"    (translation attempt {attempt + 1}/4 failed: {e})")
            time.sleep(5)
    return None


def load_properties(path):
    entries = []
    with open(path, encoding="utf-8") as fh:
        for line in fh:
            line = line.rstrip("\n")
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            entries.append((key, value))
    return entries


def escape_value(v):
    # Properties files: keep it simple, just guard backslashes -
    # values here are short UI strings, not free-form paragraphs.
    return v.replace("\\", "\\\\")


def main():
    entries = load_properties(MESSAGES_FILE)
    print(f"{len(entries)} UI strings loaded from {MESSAGES_FILE}", flush=True)

    for lang in TARGET_LANGUAGES:
        lang = lang.strip()
        out_path = f"messages_{lang}.properties"
        print(f"\n=== Translating UI strings into '{lang}' ===", flush=True)
        skipped = []
        with open(out_path, "w", encoding="utf-8") as out:
            for i, (key, value) in enumerate(entries, 1):
                if key in EXCLUDE_FROM_TRANSLATION:
                    out.write(f"{key}={escape_value(value)}\n")
                    continue
                translated = translate_text(value, lang)
                if translated is None:
                    print(f"  [{i}/{len(entries)}] SKIPPED (all retries failed): {key}")
                    skipped.append(key)
                    # fall back to English rather than leaving the key missing
                    out.write(f"{key}={escape_value(value)}\n")
                    continue
                print(f"  [{i}/{len(entries)}] {key}: '{value[:40]}' -> '{translated[:40]}'")
                out.write(f"{key}={escape_value(translated)}\n")
        if skipped:
            print(f"\n{len(skipped)} key(s) skipped and left in English for '{lang}':")
            for k in skipped:
                print(f"  - {k}")
        else:
            print(f"\nAll {len(entries)} strings translated for '{lang}'.")

    print("\nDone.")


if __name__ == "__main__":
    main()
