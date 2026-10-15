// Adds a show/hide eye button to every password box, so a typo in a
// password is easy to spot. Text comes from window.APP_I18N (set in the
// layout); the English is only a fallback.
(function () {
    function t(key, fallback) {
        return (window.APP_I18N && window.APP_I18N[key]) || fallback;
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('input[type="password"]').forEach(function (input) {
            if (input.closest('.password-field')) {
                return;
            }
            const wrap = document.createElement('div');
            wrap.className = 'password-field';
            input.parentNode.insertBefore(wrap, input);
            wrap.appendChild(input);

            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'password-toggle';
            button.setAttribute('aria-label', t('showPassword', 'Show password'));
            button.innerHTML = '<i class="fa-solid fa-eye" aria-hidden="true"></i>';
            wrap.appendChild(button);

            button.addEventListener('click', function () {
                const show = input.type === 'password';
                input.type = show ? 'text' : 'password';
                button.setAttribute('aria-label', show ? t('hidePassword', 'Hide password') : t('showPassword', 'Show password'));
                button.innerHTML = '<i class="fa-solid ' + (show ? 'fa-eye-slash' : 'fa-eye') + '" aria-hidden="true"></i>';
            });
        });
    });
})();
