// Login / register extras: soft sound effects (generated in the browser, no
// audio files), a password strength meter, Caps Lock warning, and a little
// confetti when a success message is shown. Everything is optional - the forms
// work the same without it. Sounds only play after the visitor has clicked or
// typed on the page (browsers require that) and can be switched off with the
// speaker button; the choice is remembered.
(function () {
  var card = document.querySelector('.auth-card[data-auth]');
  if (!card) return;
  var calm = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // ---- sound ---------------------------------------------------------------
  var KEY = 'bookstore-sfx';
  var on = true;
  try { on = localStorage.getItem(KEY) !== 'off'; } catch (e) { /* storage blocked */ }
  var ctx = null;

  function audio() {
    if (!on) return null;
    try {
      if (!ctx) {
        var AC = window.AudioContext || window.webkitAudioContext;
        if (!AC) return null;
        ctx = new AC();
      }
      if (ctx.state === 'suspended') ctx.resume();
      return ctx;
    } catch (e) { return null; }
  }
  function tone(freq, delay, dur, type, vol) {
    var c = audio();
    if (!c) return;
    var t0 = c.currentTime + delay;
    var osc = c.createOscillator();
    var g = c.createGain();
    osc.type = type || 'sine';
    osc.frequency.setValueAtTime(freq, t0);
    g.gain.setValueAtTime(0.0001, t0);
    g.gain.exponentialRampToValueAtTime(vol || 0.05, t0 + 0.012);
    g.gain.exponentialRampToValueAtTime(0.0001, t0 + dur);
    osc.connect(g); g.connect(c.destination);
    osc.start(t0); osc.stop(t0 + dur + 0.02);
  }
  var sfx = {
    pop:   function () { tone(660, 0, 0.09); tone(990, 0.06, 0.12); },
    tick:  function () { tone(1200, 0, 0.04, 'triangle', 0.025); },
    up:    function (n) { tone(440 + n * 110, 0, 0.12, 'sine', 0.045); },
    chime: function () { [523, 659, 784, 1047].forEach(function (f, i) { tone(f, i * 0.09, 0.35, 'sine', 0.05); }); },
    whoosh: function () { tone(300, 0, 0.18, 'sawtooth', 0.012); tone(520, 0.05, 0.2, 'triangle', 0.02); }
  };

  var toggle = document.getElementById('auth-sfx-toggle');
  function paintToggle() {
    if (!toggle) return;
    toggle.setAttribute('aria-pressed', on ? 'true' : 'false');
    var label = on ? toggle.dataset.on : toggle.dataset.off;
    toggle.title = label; toggle.setAttribute('aria-label', label);
    toggle.querySelector('i').className = 'fa-solid ' + (on ? 'fa-volume-high' : 'fa-volume-xmark');
    toggle.classList.toggle('is-off', !on);
  }
  paintToggle();
  if (toggle) {
    toggle.addEventListener('click', function () {
      on = !on;
      try { localStorage.setItem(KEY, on ? 'on' : 'off'); } catch (e) { /* storage blocked */ }
      paintToggle();
      if (on) sfx.pop();
    });
  }

  // button hover tick, submit whoosh
  card.querySelectorAll('.btn-auth, .btn-outline-brass').forEach(function (b) {
    b.addEventListener('mouseenter', sfx.tick);
  });
  var form = card.querySelector('form');
  if (form) form.addEventListener('submit', sfx.whoosh);

  // a field that has just become valid gets a check + a soft pop
  function watchValid(input) {
    var wrap = input.closest('.auth-field');
    var was = false;
    function check() {
      var ok = input.value.length > 0 && input.checkValidity();
      if (wrap) wrap.classList.toggle('is-ok', ok);
      if (ok && !was) sfx.pop();
      was = ok;
    }
    input.addEventListener('input', check);
    check();
  }
  if (card.dataset.auth === 'register') {
    ['username', 'email'].forEach(function (id) {
      var el = document.getElementById(id);
      if (el) watchValid(el);
    });
  }

  // ---- password strength (register) --------------------------------------
  var meter = document.getElementById('auth-strength');
  var pw = document.getElementById('password');
  if (meter && pw && card.dataset.auth === 'register') {
    var bars = meter.querySelectorAll('.auth-strength-bars i');
    var label = meter.querySelector('.auth-strength-label');
    var names = ['', meter.dataset.weak, meter.dataset.fair, meter.dataset.good, meter.dataset.strong];
    var lastLevel = 0;
    pw.addEventListener('input', function () {
      var v = pw.value;
      meter.hidden = v.length === 0;
      var score = 0;
      if (v.length >= 8) score++;
      if (v.length >= 12) score++;
      if (/[a-z]/.test(v) && /[A-Z]/.test(v)) score++;
      if (/\d/.test(v)) score++;
      if (/[^A-Za-z0-9]/.test(v)) score++;
      var level = v.length === 0 ? 0 : (v.length < 8 ? 1 : Math.min(4, Math.max(1, score - 0)));
      if (v.length >= 8 && level < 2) level = 2;
      meter.dataset.level = level;
      bars.forEach(function (b, i) { b.classList.toggle('on', i < level); });
      label.textContent = names[level] || '';
      var wrap = pw.closest('.auth-field');
      if (wrap) wrap.classList.toggle('is-ok', v.length >= 8);
      if (level > lastLevel && level > 1) sfx.up(level);
      lastLevel = level;
    });
  }

  // ---- Caps Lock warning (login) -------------------------------------------
  var caps = document.getElementById('auth-caps');
  if (caps && pw) {
    function capsState(e) {
      if (e.getModifierState) caps.hidden = !e.getModifierState('CapsLock');
    }
    pw.addEventListener('keydown', capsState);
    pw.addEventListener('keyup', capsState);
    pw.addEventListener('blur', function () { caps.hidden = true; });
  }

  // ---- confetti on a success message ---------------------------------------
  if (!calm && card.querySelector('.alert-success')) {
    var colors = ['#e3ac4f', '#a5433f', '#6b4816', '#fffcf5', '#9c6b24'];
    var layer = document.createElement('div');
    layer.className = 'confetti-layer';
    layer.setAttribute('aria-hidden', 'true');
    for (var i = 0; i < 36; i++) {
      var p = document.createElement('span');
      p.style.left = (Math.random() * 100) + '%';
      p.style.background = colors[i % colors.length];
      p.style.setProperty('--dx', (Math.random() * 120 - 60) + 'px');
      p.style.setProperty('--rot', (Math.random() * 720 - 360) + 'deg');
      p.style.animationDelay = (Math.random() * 0.4) + 's';
      p.style.animationDuration = (1.4 + Math.random() * 1.1) + 's';
      layer.appendChild(p);
    }
    document.body.appendChild(layer);
    setTimeout(function () { layer.remove(); }, 3200);
  }
})();
