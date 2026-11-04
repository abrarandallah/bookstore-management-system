// Manage Users: show/hide the add-user form and filter the rows on this page.
(function () {
  var toggle = document.getElementById('add-user-toggle');
  var panel = document.getElementById('add-user-panel');
  if (toggle && panel) {
    toggle.addEventListener('click', function () {
      var open = panel.hidden;
      panel.hidden = !open;
      toggle.setAttribute('aria-expanded', String(open));
      if (open) { var f = document.getElementById('nu-username'); if (f) f.focus(); }
    });
  }
  var search = document.getElementById('user-search');
  if (search) {
    search.addEventListener('input', function () {
      var q = search.value.trim().toLowerCase();
      document.querySelectorAll('.user-row').forEach(function (r) {
        r.hidden = q && r.getAttribute('data-search').indexOf(q) === -1;
      });
    });
  }
})();
