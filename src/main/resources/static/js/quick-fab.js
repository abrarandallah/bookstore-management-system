// Floating quick-actions button on the book list: toggles a small menu
// (Surprise Me, Genres, ...). Closes on outside click, Escape, or choosing an item.
(function () {
  var fab = document.getElementById('quick-fab');
  var btn = document.getElementById('quick-fab-btn');
  var menu = document.getElementById('quick-fab-menu');
  if (!fab || !btn || !menu) return;
  document.body.classList.add('has-fab');

  function setOpen(open) {
    menu.hidden = !open;
    btn.setAttribute('aria-expanded', open ? 'true' : 'false');
  }
  btn.addEventListener('click', function () { setOpen(menu.hidden); });
  var top = document.getElementById('quick-fab-top');
  if (top) {
    top.addEventListener('click', function () {
      var calm = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      window.scrollTo({ top: 0, behavior: calm ? 'auto' : 'smooth' });
      setOpen(false);
    });
  }
  document.addEventListener('click', function (e) {
    if (!fab.contains(e.target)) setOpen(false);
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && !menu.hidden) { setOpen(false); btn.focus(); }
  });
})();
