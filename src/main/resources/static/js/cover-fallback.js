// Book covers whose image file is missing on the server (for example after the
// uploads folder was wiped) would otherwise show a broken-image icon with the
// word "Cover". This swaps each one for a quiet book-icon placeholder instead.
(function () {
    function replaceWithPlaceholder(img) {
        if (img.dataset.coverFallbackDone) {
            return;
        }
        img.dataset.coverFallbackDone = '1';
        const box = document.createElement('div');
        box.className = 'cover-placeholder ' + (img.className || '');
        box.setAttribute('role', 'img');
        box.setAttribute('aria-label', img.getAttribute('alt') || '');
        box.innerHTML = '<i class="fa-solid fa-book-open" aria-hidden="true"></i>';
        img.replaceWith(box);
    }

    function isCover(el) {
        return el && el.tagName === 'IMG' && el.matches('img.book-cover, img.cover-img');
    }

    // Images that fail after this script has loaded.
    document.addEventListener('error', function (event) {
        if (isCover(event.target)) {
            replaceWithPlaceholder(event.target);
        }
    }, true);

    // Images that already failed before this script ran.
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('img.book-cover, img.cover-img').forEach(function (img) {
            if (img.complete && img.naturalWidth === 0) {
                replaceWithPlaceholder(img);
            }
        });
    });
})();
