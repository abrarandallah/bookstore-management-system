// Collapses the book list's genre chips to two rows, with a "Show all genres"
// toggle. The chip row lives inside #book-results, which live search replaces,
// so the click handler is delegated and the overflow check re-runs on every swap.
(function () {
  var results = document.getElementById('book-results');
  if (!results) return;

  function refresh() {
    var wrap = results.querySelector('.genre-filter-wrap');
    if (!wrap) return;
    var row = wrap.querySelector('.genre-filter-row');
    var btn = wrap.querySelector('.genre-more');
    var wasOpen = wrap.classList.contains('is-open');
    wrap.classList.add('is-open'); // measure the full height
    var full = row.scrollHeight;
    wrap.classList.toggle('is-open', wasOpen);
    var chip = row.querySelector('.genre-filter');
    var twoRows = chip ? (chip.offsetHeight * 2 + 12) : 0;
    wrap.classList.toggle('has-overflow', full > twoRows + 4);
    btn.setAttribute('aria-expanded', wasOpen ? 'true' : 'false');
  }

  results.addEventListener('click', function (e) {
    var btn = e.target.closest('.genre-more');
    if (!btn) return;
    var wrap = btn.closest('.genre-filter-wrap');
    var open = wrap.classList.toggle('is-open');
    btn.textContent = open ? btn.dataset.less : btn.dataset.more;
    btn.setAttribute('aria-expanded', open ? 'true' : 'false');
  });

  new MutationObserver(refresh).observe(results, { childList: true });
  refresh();
})();
