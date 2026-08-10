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

    // Kept in sync with the URL on load so a bookmarked/back-navigated
    // link to page 3 (say) starts the JS-driven state on page 3 too,
    // rather than silently resetting to page 1 on the next AJAX search.
    let currentPage = parseInt(new URLSearchParams(window.location.search).get('page'), 10) || 1;

    function currentParams() {
        const params = new URLSearchParams();
        if (input.value) {
            params.set('q', input.value);
        }
        params.set('sort', sortSelect.value);
        if (genreSelect && genreSelect.value) {
            params.set('genre', genreSelect.value);
        }
        if (currentPage > 1) {
            params.set('page', currentPage);
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
        currentPage = 1;
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => runSearch(), DEBOUNCE_MS);
    });

    sortSelect.addEventListener('change', () => {
        currentPage = 1;
        runSearch();
    });

    if (genreSelect) {
        genreSelect.addEventListener('change', () => {
            currentPage = 1;
            runSearch();
        });
    }

    if (clearLink) {
        clearLink.addEventListener('click', (event) => {
            event.preventDefault();
            input.value = '';
            currentPage = 1;
            updateClearLink();
            runSearch();
        });
    }

    // Pagination links live inside #book-results, which gets its
    // innerHTML replaced on every search - so the listener goes on the
    // stable container itself (event delegation) rather than on the links
    // directly, which would stop working the moment the fragment refreshes.
    results.addEventListener('click', (event) => {
        const link = event.target.closest('.page-link[data-page]');
        if (!link) {
            return;
        }
        event.preventDefault();
        const page = parseInt(link.dataset.page, 10);
        if (!page || page === currentPage) {
            return;
        }
        currentPage = page;
        runSearch();
        results.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });

    form.addEventListener('submit', (event) => {
        // The live search already re-runs on every keystroke, so an actual
        // full-page submit (e.g. from pressing Enter) would just be a
        // slower version of what already happened - intercept it too.
        event.preventDefault();
        clearTimeout(debounceTimer);
        currentPage = 1;
        runSearch();
    });

    updateClearLink();
})();