// First-visit language popup. The popup's markup only exists in the page
// at all when the server decided to render it (see showLanguagePopup in
// LanguagePopupModelAdvice) - this script just handles fading it in and
// wiring up dismissal, it never decides on its own whether to show it.
(function () {
    const overlay = document.getElementById('language-popup-overlay');
    if (!overlay) {
        return;
    }

    // Same reflow-before-transition trick as confirm-modal.js, so the
    // fade-in still animates instead of the overlay just appearing.
    void overlay.offsetWidth;
    overlay.classList.add('is-visible');

    function dismiss() {
        overlay.classList.remove('is-visible');
        setTimeout(() => overlay.remove(), 250);
        // Session-only cookie (see LanguageController#dismiss) - the popup
        // will show again on a later visit, just not for the rest of this
        // one. Fire-and-forget: nothing in the current page depends on the
        // response, and a failed request just means the popup might show
        // again sooner than intended, not a broken page.
        fetch('/language/dismiss', { method: 'GET', credentials: 'same-origin' }).catch(() => {});
    }

    const closeButton = overlay.querySelector('.language-popup-close');
    if (closeButton) {
        closeButton.addEventListener('click', dismiss);
    }
    overlay.addEventListener('click', (event) => {
        if (event.target === overlay) {
            dismiss();
        }
    });
    document.addEventListener('keydown', function onKeydown(event) {
        if (event.key === 'Escape') {
            dismiss();
            document.removeEventListener('keydown', onKeydown);
        }
    });
})();
