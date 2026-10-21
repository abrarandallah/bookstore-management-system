(function () {
  var grid = document.getElementById('genre-grid');
  if (!grid) return;
  var cols = Array.prototype.slice.call(grid.querySelectorAll('.genre-col'));
  var filter = document.getElementById('genre-filter');
  var empty = document.getElementById('genre-empty');
  var buttons = document.querySelectorAll('.genre-sort-btn');
  var lang = document.documentElement.lang || undefined;
  var max = 1;
  cols.forEach(function (c) { max = Math.max(max, parseInt(c.dataset.count, 10) || 0); });
  cols.forEach(function (c) {
    var pct = Math.max(4, Math.round(((parseInt(c.dataset.count, 10) || 0) / max) * 100));
    c.querySelector('.genre-bar-fill').style.width = pct + '%';
  });

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
