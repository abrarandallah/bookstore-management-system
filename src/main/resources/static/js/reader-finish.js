// The end-of-book celebration for visitors without an account (see the
// #guest-finish block in bookRead.html). bookRead calls window.showGuestFinish().
(function () {
  var overlay = document.getElementById('guest-finish');
  if (!overlay) return;
  var calm = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var lastFocus = null;

  function confetti() {
    if (calm) return;
    var colors = ['#e3ac4f', '#a3394a', '#5a1d22', '#fffafa', '#9c6b24'];
    var layer = document.createElement('div');
    layer.className = 'confetti-layer';
    layer.setAttribute('aria-hidden', 'true');
    for (var i = 0; i < 46; i++) {
      var p = document.createElement('span');
      p.style.left = (Math.random() * 100) + '%';
      p.style.background = colors[i % colors.length];
      p.style.setProperty('--dx', (Math.random() * 140 - 70) + 'px');
      p.style.setProperty('--rot', (Math.random() * 720 - 360) + 'deg');
      p.style.animationDelay = (Math.random() * 0.4) + 's';
      p.style.animationDuration = (1.4 + Math.random() * 1.2) + 's';
      layer.appendChild(p);
    }
    document.body.appendChild(layer);
    setTimeout(function () { layer.remove(); }, 3400);
  }

  function open() {
    lastFocus = document.activeElement;
    overlay.hidden = false;
    document.body.classList.add('finish-open');
    var primary = document.getElementById('guest-finish-primary');
    if (primary) primary.focus();
    confetti();
  }
  function close() {
    overlay.hidden = true;
    document.body.classList.remove('finish-open');
    if (lastFocus && lastFocus.focus) lastFocus.focus();
  }

  window.showGuestFinish = open;
  document.getElementById('guest-finish-close').addEventListener('click', close);
  document.getElementById('guest-finish-keep').addEventListener('click', close);
  overlay.addEventListener('click', function (e) { if (e.target === overlay) close(); });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && !overlay.hidden) close();
  });
})();
