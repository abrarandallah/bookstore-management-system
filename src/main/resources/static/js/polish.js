// Small site-wide touches, all optional:
//  - a thin progress bar at the top while the next page loads
//  - sections fade up as they scroll into view
//  - the home page photo tilts gently with the mouse
(function () {
  var calm = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // 1. navigation progress bar
  var bar = document.createElement('div');
  bar.className = 'nav-progress';
  bar.setAttribute('aria-hidden', 'true');
  document.body.appendChild(bar);
  function startBar() {
    bar.classList.remove('is-done');
    bar.style.width = '0%';
    void bar.offsetWidth;
    bar.classList.add('is-running');
    bar.style.width = '82%';
  }
  document.addEventListener('click', function (e) {
    var a = e.target.closest && e.target.closest('a[href]');
    if (!a || e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
    if (a.target && a.target !== '_self') return;
    if (a.hasAttribute('download') || a.hasAttribute('data-page') || a.getAttribute('href').charAt(0) === '#') return;
    if (a.origin !== window.location.origin) return;
    startBar();
  });
  window.addEventListener('pageshow', function () {
    bar.classList.remove('is-running');
    bar.classList.add('is-done');
  });

  // 2. scroll reveal (only things that start below the fold)
  if (!calm && 'IntersectionObserver' in window) {
    var targets = document.querySelectorAll('.home-stats, .home-marquee-wrap, .review-card, .form-panel, .card, .empty-state, .daily-card');
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) { en.target.classList.add('is-revealed'); io.unobserve(en.target); }
      });
    }, { threshold: 0.12 });
    targets.forEach(function (t) {
      var r = t.getBoundingClientRect();
      if (r.top > window.innerHeight * 0.92) {
        t.classList.add('reveal');
        io.observe(t);
      }
    });
  }

  // 3. hero photo tilt
  var fig = document.querySelector('.hero-figure');
  var img = fig && fig.querySelector('img');
  if (img && !calm && window.matchMedia('(hover: hover) and (pointer: fine)').matches) {
    fig.addEventListener('pointermove', function (e) {
      var r = fig.getBoundingClientRect();
      var x = (e.clientX - r.left) / r.width - 0.5;
      var y = (e.clientY - r.top) / r.height - 0.5;
      img.style.transition = 'transform 0.08s linear';
      img.style.transform = 'perspective(900px) rotateY(' + (x * 9) + 'deg) rotateX(' + (-y * 9) + 'deg) rotate(-2deg) scale(1.02)';
    });
    fig.addEventListener('pointerleave', function () {
      img.style.transition = 'transform 0.4s ease';
      img.style.transform = '';
    });
  }
})();
