// Replaces the book list's GET-form-submit search with a debounced
// fetch() that re-renders just the results fragment. Falls back to a plain
// full-page form submission if JS fails or is disabled, since the <form>
// still has a real action/method.
(function () {
    const form = document.getElementById('search-form');
    if (!form) {
        return;
    }

    const input = document.getElementById('search-input');
    const sortSelect = document.getElementById('search-sort');
    const genreSelect = document.getElementById('search-genre');
    const results = document.getElementById('book-results');
    const clearLink = document.getElementById('search-clear');

    const DEBOUNCE_MS = 300;
    let debounceTimer = null;
    let activeController = null;

    function currentParams() {
        const params = new URLSearchParams();
        if (input.value) {
            params.set('q', input.value);
        }
        params.set('sort', sortSelect.value);
        if (genreSelect && genreSelect.value) {
            params.set('genre', genreSelect.value);
        }
        return params;
    }

    function updateClearLink() {
        if (!clearLink) {
            return;
        }
        const hasQuery = input.value.trim().length > 0;
        clearLink.classList.toggle('d-none', !hasQuery);
    }

    function runSearch({ pushHistory = true } = {}) {
        const params = currentParams();

        if (activeController) {
            activeController.abort();
        }
        activeController = new AbortController();

        form.classList.add('is-loading');
        results.classList.add('search-results-updating');

        fetch('/available_books/results?' + params.toString(), {
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            signal: activeController.signal,
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error('Search request failed: ' + response.status);
                }
                return response.text();
            })
            .then((html) => {
                results.innerHTML = html;
                if (pushHistory) {
                    const url = '/available_books' + (params.toString() ? '?' + params.toString() : '');
                    window.history.replaceState(null, '', url);
                }
            })
            .catch((err) => {
                if (err.name !== 'AbortError') {
                    // Live search failed silently rather than breaking the
                    // page - the plain form submit below is always still
                    // available as a fallback.
                    console.error(err);
                }
            })
            .finally(() => {
                form.classList.remove('is-loading');
                results.classList.remove('search-results-updating');
            });
    }

    input.addEventListener('input', () => {
        updateClearLink();
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => runSearch(), DEBOUNCE_MS);
    });

    sortSelect.addEventListener('change', () => runSearch());

    if (genreSelect) {
        genreSelect.addEventListener('change', () => runSearch());
    }

    if (clearLink) {
        clearLink.addEventListener('click', (event) => {
            event.preventDefault();
            input.value = '';
            updateClearLink();
            runSearch();
        });
    }

    form.addEventListener('submit', (event) => {
        // The live search already re-runs on every keystroke, so an actual
        // full-page submit (e.g. from pressing Enter) would just be a
        // slower version of what already happened - intercept it too.
        event.preventDefault();
        clearTimeout(debounceTimer);
        runSearch();
    });

    updateClearLink();
})();