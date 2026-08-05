// Lightweight toast notifications, plus an AJAX handler for the "Add to My
// Books" bookmark form so it gives instant feedback instead of a full page
// reload with no visible confirmation.
(function () {
    function getStack() {
        let stack = document.querySelector('.toast-stack');
        if (!stack) {
            stack = document.createElement('div');
            stack.className = 'toast-stack';
            stack.setAttribute('role', 'status');
            stack.setAttribute('aria-live', 'polite');
            document.body.appendChild(stack);
        }
        return stack;
    }

    // icon: a Font Awesome class suffix, e.g. "circle-check".
    window.showToast = function (message, { icon = 'circle-check', isError = false, duration = 3200 } = {}) {
        const stack = getStack();
        const note = document.createElement('div');
        note.className = 'toast-note' + (isError ? ' toast-error' : '');
        note.innerHTML = '<i class="fa-solid fa-' + icon + '"></i><span></span>';
        note.querySelector('span').textContent = message;
        stack.appendChild(note);

        // Next frame, so the transition actually runs instead of starting
        // already-visible.
        requestAnimationFrame(() => note.classList.add('is-visible'));

        setTimeout(() => {
            note.classList.remove('is-visible');
            note.addEventListener('transitionend', () => note.remove(), { once: true });
        }, duration);
    };

    function handleBookmarkSubmit(form) {
        const button = form.querySelector('button[type="submit"]');
        if (button) {
            button.disabled = true;
        }

        fetch(form.action, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: new FormData(form),
            redirect: 'manual',
        })
            .then((response) => {
                // A same-origin redirect (the normal success path) comes
                // back as an opaque redirect (status 0, type "opaqueredirect")
                // when redirect: 'manual' is used - that's success, not an
                // error, so treat it as such rather than throwing.
                if (response.ok || response.type === 'opaqueredirect' || response.status === 0) {
                    window.showToast('Added to My Books', { icon: 'bookmark' });
                } else {
                    window.showToast('Could not add that book - try again', { icon: 'triangle-exclamation', isError: true });
                }
            })
            .catch(() => {
                window.showToast('Could not add that book - try again', { icon: 'triangle-exclamation', isError: true });
            })
            .finally(() => {
                if (button) {
                    button.disabled = false;
                }
            });
    }

    // Event delegation, so this keeps working after the search results
    // fragment is swapped out by live-search.js.
    document.addEventListener('submit', (event) => {
        const form = event.target.closest('.ajax-bookmark-form');
        if (!form) {
            return;
        }
        event.preventDefault();
        handleBookmarkSubmit(form);
    });
})();