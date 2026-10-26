(function () {
  var grid = document.getElementById('genre-grid');
  if (!grid) return;
  var cols = Array.prototype.slice.call(grid.querySelectorAll('.genre-col'));
  var filter = document.getElementById('genre-filter');
  var empty = document.getElementById('genre-empty');
  var buttons = document.querySelectorAll('.genre-sort-btn');
  var lang = document.documentElement.lang || undefined;
  var calm = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // Size bars: start empty, then grow to their share of the biggest genre.
  var max = 1;
  cols.forEach(function (c) { max = Math.max(max, parseInt(c.dataset.count, 10) || 0); });
  function widthFor(c) {
    return Math.max(6, Math.round(((parseInt(c.dataset.count, 10) || 0) / max) * 100)) + '%';
  }
  cols.forEach(function (c) { c.querySelector('.genre-bar-fill').style.width = calm ? widthFor(c) : '0%'; });
  if (!calm) {
    requestAnimationFrame(function () {
      requestAnimationFrame(function () {
        cols.forEach(function (c) { c.querySelector('.genre-bar-fill').style.width = widthFor(c); });
      });
    });
  }

  // "Popular right now": the five biggest genres as colourful chips.
  var pop = document.getElementById('genre-popular');
  if (pop) {
    var top = cols.slice().sort(function (a, b) {
      return (parseInt(b.dataset.count, 10) || 0) - (parseInt(a.dataset.count, 10) || 0);
    }).slice(0, 5);
    var list = pop.querySelector('.genres-popular-list');
    top.forEach(function (c) {
      var a = document.createElement('a');
      a.className = 'genres-popular-chip';
      a.style.setProperty('--h', c.style.getPropertyValue('--h'));
      a.href = c.querySelector('a').getAttribute('href');
      a.textContent = c.dataset.label;
      list.appendChild(a);
    });
    pop.querySelector('.genres-popular-label').textContent = pop.dataset.label;
    pop.hidden = top.length === 0;
  }

  // "Pick one for me": jump to a random genre.
  var lucky = document.getElementById('genre-lucky');
  if (lucky) {
    lucky.addEventListener('click', function () {
      var pick = cols[Math.floor(Math.random() * cols.length)];
      window.location.href = pick.querySelector('a').getAttribute('href');
    });
  }

  var mode = 'az';
  function render() {
    var q = (filter.value || '').trim().toLowerCase();
    var sorted = cols.slice().sort(function (a, b) {
      if (mode === 'most') {
        var d = (parseInt(b.dataset.count, 10) || 0) - (parseInt(a.dataset.count, 10) || 0);
        if (d !== 0) return d;
      }
      return a.dataset.label.localeCompare(b.dataset.label, lang);
    });
    var shown = 0;
    sorted.forEach(function (c) {
      var match = !q || c.dataset.name.indexOf(q) !== -1;
      c.classList.toggle('d-none', !match);
      if (match) shown++;
      grid.appendChild(c);
    });
    empty.classList.toggle('d-none', shown !== 0);
  }

  filter.addEventListener('input', render);
  buttons.forEach(function (btn) {
    btn.addEventListener('click', function () {
      mode = btn.dataset.sort;
      buttons.forEach(function (b) {
        var on = b === btn;
        b.classList.toggle('is-active', on);
        b.setAttribute('aria-pressed', on ? 'true' : 'false');
      });
      render();
    });
  });
  render();
})();
