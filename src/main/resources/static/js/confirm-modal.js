// Replaces native window.confirm()/alert() with a themed modal, so
// destructive actions (delete book, delete account, etc.) match the rest of
// the app instead of looking like a browser/OS error dialog.
(function () {
    // Resolves true/false depending on which button is clicked, backdrop
    // click or Escape both counting as cancel.
    function showConfirmModal(message, { gentle = false, confirmLabel = 'Delete', cancelLabel = 'Cancel' } = {}) {
        return new Promise((resolve) => {
            const overlay = document.createElement('div');
            overlay.className = 'confirm-overlay';
            overlay.innerHTML =
                '<div class="confirm-card' + (gentle ? ' is-gentle' : '') + '" role="alertdialog" aria-modal="true">' +
                '<div class="confirm-icon"><i class="fa-solid ' + (gentle ? 'fa-circle-question' : 'fa-triangle-exclamation') + '"></i></div>' +
                '<p></p>' +
                '<div class="confirm-actions">' +
                '<button type="button" class="btn btn-secondary confirm-cancel"></button>' +
                '<button type="button" class="btn ' + (gentle ? 'btn-primary' : 'btn-danger') + ' confirm-ok"></button>' +
                '</div></div>';
            overlay.querySelector('p').textContent = message;
            overlay.querySelector('.confirm-cancel').textContent = cancelLabel;
            overlay.querySelector('.confirm-ok').textContent = confirmLabel;
            document.body.appendChild(overlay);
            requestAnimationFrame(() => overlay.classList.add('is-visible'));

            function close(result) {
                overlay.classList.remove('is-visible');
                // Remove the overlay once, however we get there. Some
                // browsers/environments (notably prefers-reduced-motion,
                // which forces a near-zero transition-duration in this
                // app's CSS) never fire transitionend for a transition
                // that short - without this fallback the invisible,
                // full-screen overlay would stay in the DOM and silently
                // block every click on the page until the next navigation.
                let removed = false;
                function removeOverlay() {
                    if (removed) return;
                    removed = true;
                    overlay.remove();
                }
                overlay.addEventListener('transitionend', removeOverlay, { once: true });
                setTimeout(removeOverlay, 250);
                document.removeEventListener('keydown', onKeydown);
                resolve(result);
            }
            function onKeydown(event) {
                if (event.key === 'Escape') {
                    close(false);
                }
            }

            overlay.querySelector('.confirm-ok').addEventListener('click', () => close(true));
            overlay.querySelector('.confirm-cancel').addEventListener('click', () => close(false));
            overlay.addEventListener('click', (event) => {
                if (event.target === overlay) {
                    close(false);
                }
            });
            document.addEventListener('keydown', onKeydown);
        });
    }

    // Called from onsubmit="return appConfirm(event, 'message')". Always
    // returns false to block the native synchronous submit; if the user
    // confirms, resubmits the same form programmatically (which does not
    // re-trigger onsubmit, so this can't loop).
    window.appConfirm = function (event, message, options = {}) {
        event.preventDefault();
        const form = event.target;
        showConfirmModal(message, options).then((confirmed) => {
            if (confirmed) {
                form.submit();
            }
        });
        return false;
    };
})();