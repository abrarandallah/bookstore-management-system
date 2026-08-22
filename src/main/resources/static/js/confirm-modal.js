// Replaces native window.confirm()/alert() with a themed modal, so
// destructive actions (delete book, delete account, etc.) match the rest of
// the app instead of looking like a browser/OS error dialog.
(function () {
    // Defensive: if a previous overlay somehow never got cleaned up (see
    // the removal-reliability note in close() below), it would sit there
    // full-screen and invisible, silently swallowing every click on the
    // page - looking exactly like "the button does nothing" with no error
    // anywhere. Clearing any stray overlay before creating a new one means
    // that state can't accumulate across multiple attempts.
    function clearStrayOverlays() {
        document.querySelectorAll('.confirm-overlay').forEach((el) => el.remove());
    }

    // Resolves true/false depending on which button is clicked, backdrop
    // click or Escape both counting as cancel.
    function showConfirmModal(message, { gentle = false, confirmLabel = 'Delete', cancelLabel = 'Cancel' } = {}) {
        clearStrayOverlays();
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
            // Forces the browser to register the opacity:0 starting state
            // before the "is-visible" transition target is applied, so the
            // fade-in still animates - reading a layout property like this
            // triggers a synchronous reflow, which (unlike
            // requestAnimationFrame) still happens even in a backgrounded/
            // hidden browser tab.
            void overlay.offsetWidth;
            overlay.classList.add('is-visible');

            let removed = false;
            function removeOverlay() {
                if (removed) return;
                removed = true;
                overlay.remove();
            }

            function close(result) {
                overlay.classList.remove('is-visible');
                // Same reasoning as above: don't gate actual removal on
                // transitionend, which can also be delayed or dropped
                // (backgrounded tab, prefers-reduced-motion collapsing the
                // transition to ~0 which some browsers fire inconsistently
                // for, etc.). It still fires the fade-out when available;
                // removeOverlay()'s own `removed` guard makes running it
                // twice (once from transitionend, once from the timeout)
                // harmless either way.
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