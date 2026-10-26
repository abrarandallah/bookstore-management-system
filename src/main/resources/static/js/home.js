// Home page extras that need no account:
//  1. numbers count up when they scroll into view
//  2. "Continue reading" chip, from the last book this browser was reading
(function () {
  var calm = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // 1. count-up
  var nums = document.querySelectorAll('.home-stat-num');
  function run(el) {
    var target = parseInt(el.getAttribute('data-count'), 10) || 0;
    if (calm || target < 2) { el.textContent = target; return; }
    var start = null, dur = 1100;
    function step(t) {
      if (start === null) start = t;
      var p = Math.min((t - start) / dur, 1);
      var eased = 1 - Math.pow(1 - p, 3);
      el.textContent = Math.round(target * eased);
      if (p < 1) requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
  }
  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (e.isIntersecting) { run(e.target); io.unobserve(e.target); }
      });
    }, { threshold: 0.6 });
    nums.forEach(function (n) { io.observe(n); });
  }

  // 2. continue chip
  var chip = document.getElementById('home-continue');
  if (!chip) return;
  try {
    var saved = JSON.parse(localStorage.getItem('bookstore-last-read') || 'null');
    if (!saved || !saved.id || !saved.name) return;
    var progress = (chip.dataset.progress || '').replace('{0}', saved.step).replace('{1}', saved.total);
    chip.href = '/available_books/' + encodeURIComponent(saved.id) + '/read';
    chip.textContent = '';
    var icon = document.createElement('i');
    icon.className = 'fa-solid fa-bookmark';
    icon.setAttribute('aria-hidden', 'true');
    var label = document.createElement('strong');
    label.textContent = chip.dataset.label;
    var rest = document.createElement('span');
    rest.textContent = saved.name + ' \u00b7 ' + progress;
    chip.append(icon, ' ', label, ' ', rest);
    chip.hidden = false;
  } catch (e) { /* storage blocked: no chip */ }
})();
