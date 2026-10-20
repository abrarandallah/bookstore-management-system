// Stops a form being submitted twice (double-click, slow connection,
// impatient Enter key) - which would otherwise create duplicate accounts,
// reviews or books. After the first submit the button shows a spinner and
// further submits are ignored until the page changes (or the user comes back
// to it with the Back button, see pageshow below).
//
// Deliberately NOT done by disabling the button: a disabled button's own
// name/value is left out of the submitted form data. Skips GET forms (search),
// forms already cancelled by another handler (e.g. the delete-confirm
// dialog), and any form marked data-no-submit-guard.
(function () {
    function release(form) {
        delete form.dataset.submitting;
        form.querySelectorAll('.is-submitting').forEach(function (b) {
            b.classList.remove('is-submitting');
            b.removeAttribute('aria-busy');
            var spinner = b.querySelector('.submit-guard-spinner');
            if (spinner) { spinner.remove(); }
        });
    }

    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!(form instanceof HTMLFormElement)) { return; }
        if ((form.method || 'get').toLowerCase() === 'get') { return; }
        if ('noSubmitGuard' in form.dataset) { return; }

        if (form.dataset.submitting) {
            event.preventDefault();
            return;
        }
        // Runs after inline onsubmit handlers, so a cancelled submit is visible here.
        if (event.defaultPrevented) { return; }

        form.dataset.submitting = 'true';
        form.querySelectorAll('button[type="submit"], button:not([type])').forEach(function (b) {
            b.classList.add('is-submitting');
            b.setAttribute('aria-busy', 'true');
            var spinner = document.createElement('i');
            spinner.className = 'fa-solid fa-circle-notch fa-spin submit-guard-spinner';
            spinner.setAttribute('aria-hidden', 'true');
            b.appendChild(spinner);
        });
    });

    // Back/forward cache can restore the page mid-"submitting" - reset it.
    window.addEventListener('pageshow', function (event) {
        if (!event.persisted) { return; }
        document.querySelectorAll('form[data-submitting]').forEach(release);
    });
})();
