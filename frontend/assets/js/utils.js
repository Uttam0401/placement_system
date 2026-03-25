// ============================================================
// Campus Placement Management System — Shared Utilities
// ============================================================

const API = 'https://placementsystem-production.up.railway.app/api';

// ===== AUTH =====
const Auth = {
  getToken  : () => localStorage.getItem('ps_token'),
  getRole   : () => localStorage.getItem('ps_role'),
  getUserId : () => localStorage.getItem('ps_userId'),
  getName   : () => localStorage.getItem('ps_name'),
  getEmail  : () => localStorage.getItem('ps_email'),

  save(data) {
    localStorage.setItem('ps_token',  data.token);
    localStorage.setItem('ps_role',   data.role);
    localStorage.setItem('ps_userId', data.userId);
    localStorage.setItem('ps_name',   data.name);
    localStorage.setItem('ps_email',  data.email || '');
  },

  clear() {
    ['ps_token','ps_role','ps_userId','ps_name','ps_email'].forEach(k => localStorage.removeItem(k));
  },

  loggedIn : () => !!localStorage.getItem('ps_token'),

  require(role) {
    if (!this.loggedIn()) { window.location.href = roleRoot() + '../auth/login.html'; return false; }
    if (role && this.getRole() !== role) {
      Toast.show('Access denied.', 'error');
      setTimeout(() => window.location.href = roleRoot() + '../auth/login.html', 1500);
      return false;
    }
    return true;
  },

  logout() { this.clear(); window.location.href = '../auth/login.html'; }
};

function roleRoot() { return ''; }

// ===== API =====
const Api = {
  async req(method, endpoint, body = null, isForm = false) {
    const headers = {};
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    if (!isForm) headers['Content-Type'] = 'application/json';

    const opts = { method, headers };
    if (body) opts.body = isForm ? body : JSON.stringify(body);

    const res = await fetch(API + endpoint, opts);
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || data.message || `HTTP ${res.status}`);
    return data;
  },

  get    : (ep)       => Api.req('GET',    ep),
  post   : (ep, body) => Api.req('POST',   ep, body),
  put    : (ep, body) => Api.req('PUT',    ep, body),
  delete : (ep)       => Api.req('DELETE', ep),
  upload : (ep, fd)   => Api.req('POST',   ep, fd, true),
};

// ===== TOAST =====
const Toast = {
  _c: null,
  _init() {
    if (!this._c) {
      this._c = document.createElement('div');
      this._c.className = 'toast-wrap';
      document.body.appendChild(this._c);
    }
  },
  show(msg, type = 'info', dur = 4000) {
    this._init();
    const icons = { success:'✅', error:'❌', warning:'⚠️', info:'ℹ️' };
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    t.innerHTML = `<span>${icons[type]||'•'}</span><span>${msg}</span>`;
    this._c.appendChild(t);
    setTimeout(() => { t.style.animation = 'slideIn .3s ease reverse'; setTimeout(() => t.remove(), 300); }, dur);
  }
};

// ===== MODAL =====
const Modal = {
  open  : id => document.getElementById(id)?.classList.add('open'),
  close : id => document.getElementById(id)?.classList.remove('open'),
  closeAll : () => document.querySelectorAll('.modal-overlay.open').forEach(m => m.classList.remove('open'))
};
document.addEventListener('click',  e => { if (e.target.classList.contains('modal-overlay')) Modal.closeAll(); });
document.addEventListener('keydown', e => { if (e.key === 'Escape') Modal.closeAll(); });

// ===== LOADER =====
const Loader = {
  show() {
    let el = document.getElementById('_loader');
    if (!el) {
      el = document.createElement('div');
      el.id = '_loader'; el.className = 'loader-overlay';
      el.innerHTML = '<div class="spinner"></div>';
      document.body.appendChild(el);
    }
    el.style.display = 'flex';
  },
  hide() { const el = document.getElementById('_loader'); if (el) el.style.display = 'none'; }
};

// ===== UI HELPERS =====
const UI = {
  set   : (id, html) => { const el = document.getElementById(id); if (el) el.innerHTML = html; },
  show  : id => { const el = document.getElementById(id); if (el) el.style.display = ''; },
  hide  : id => { const el = document.getElementById(id); if (el) el.style.display = 'none'; },

  badge(status) {
    const s  = (status||'').toLowerCase().replace(' ','_');
    const lbl = {
      pending:'⏳ Pending', approved:'✅ Approved', rejected:'❌ Rejected',
      applied:'📋 Applied', under_review:'🔍 Under Review', shortlisted:'🌟 Shortlisted',
      placed:'🏆 Placed',   active:'🟢 Active', closed:'🔴 Closed', draft:'📝 Draft'
    };
    return `<span class="badge badge-${s}">${lbl[s] || status}</span>`;
  },

  date(str) {
    if (!str) return '—';
    return new Date(str).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' });
  },

  trunc: (s, n=80) => s && s.length > n ? s.slice(0,n) + '…' : (s || ''),
  initial: name => (name||'?')[0].toUpperCase(),
  initials: name => (name||'?').split(' ').map(w=>w[0]).join('').slice(0,2).toUpperCase(),
  logoLetter: name => (name||'?')[0].toUpperCase(),

  empty(msg, icon='📭', sub='') {
    return `<div class="empty"><span class="ei">${icon}</span><h3>${msg}</h3><p>${sub}</p></div>`;
  }
};

// ===== SIDEBAR =====
function setupSidebar(roleLabel) {
  const name = Auth.getName() || 'User';

  const el = id => document.getElementById(id);
  if (el('s-name'))   el('s-name').textContent   = name;
  if (el('s-role'))   el('s-role').textContent   = roleLabel;
  if (el('s-avatar')) el('s-avatar').textContent = UI.initials(name);
  if (el('topbar-sub')) el('topbar-sub').textContent = `Welcome, ${name.split(' ')[0]}!`;

  const cur = location.pathname.split('/').pop();
  document.querySelectorAll('.nav-item[data-page]').forEach(item => {
    if (item.dataset.page === cur) item.classList.add('active');
    item.addEventListener('click', () => { const p = item.dataset.page; if (p) location.href = p; });
  });

  document.querySelectorAll('.btn-logout').forEach(b =>
    b.addEventListener('click', () => { if (confirm('Logout?')) Auth.logout(); })
  );

  // Mobile sidebar + backdrop
  const tog = document.getElementById('sidebar-toggle');
  const sb  = document.querySelector('.sidebar');
  let backdrop = document.getElementById('_sb-backdrop');
  if (!backdrop) {
    backdrop = document.createElement('div');
    backdrop.id = '_sb-backdrop';
    backdrop.className = 'sidebar-backdrop';
    document.body.appendChild(backdrop);
  }
  const closeSidebar = () => { sb?.classList.remove('open'); backdrop.classList.remove('open'); };
  if (tog && sb) {
    tog.addEventListener('click', () => {
      const isOpen = sb.classList.toggle('open');
      backdrop.classList.toggle('open', isOpen);
    });
    backdrop.addEventListener('click', closeSidebar);
  }

  // Inject dark mode toggle button into topbar-right if not present
  const tbr = document.querySelector('.topbar-right');
  if (tbr && !tbr.querySelector('.dark-toggle')) {
    const dmBtn = document.createElement('button');
    dmBtn.className = 'dark-toggle topbar-btn';
    dmBtn.title = 'Toggle dark mode';
    dmBtn.style.border = '1px solid var(--border)';
    tbr.insertBefore(dmBtn, tbr.firstChild);
    dmBtn.addEventListener('click', () => DarkMode.toggle());
  }
  DarkMode._update();
}

// ===== CHATBOT =====
function setupChatbot() {
  const bubble = document.getElementById('chatbot-btn');
  const win    = document.getElementById('chatbot-win');
  const input  = document.getElementById('chat-input');
  const send   = document.getElementById('chat-send');
  const msgs   = document.getElementById('chat-msgs');

  if (!bubble || !win) return;

  addBot('Hello! 👋 I\'m your Placement Assistant.\n\nTry asking: **jobs**, **eligibility**, **how to apply**, **status**');

  bubble.addEventListener('click', () => win.classList.toggle('open'));

  const doSend = async () => {
    const q = input.value.trim();
    if (!q) return;
    addUser(q); input.value = '';
    const tid = addBot('Typing…');
    try {
      const d = await Api.post('/chatbot/ask', { question: q });
      updateBot(tid, d.answer);
    } catch { updateBot(tid, 'Sorry, I couldn\'t connect. Please try again!'); }
  };

  send.addEventListener('click', doSend);
  input.addEventListener('keydown', e => { if (e.key === 'Enter') doSend(); });

  function addBot(text, temp=false) {
    const id = 'cm' + Date.now();
    const d = document.createElement('div');
    d.className = 'chat-msg bot'; d.id = id;
    d.innerHTML = fmt(text);
    msgs.appendChild(d); msgs.scrollTop = msgs.scrollHeight;
    return id;
  }
  function updateBot(id, text) {
    const el = document.getElementById(id);
    if (el) el.innerHTML = fmt(text);
    msgs.scrollTop = msgs.scrollHeight;
  }
  function addUser(text) {
    const d = document.createElement('div');
    d.className = 'chat-msg user'; d.textContent = text;
    msgs.appendChild(d); msgs.scrollTop = msgs.scrollHeight;
  }
  function fmt(t) { return t.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>'); }
}

// ===== DARK MODE =====
const DarkMode = {
  init() {
    const saved = localStorage.getItem('ps_theme') || 'light';
    document.documentElement.setAttribute('data-theme', saved);
    this._update();
  },
  toggle() {
    const cur = document.documentElement.getAttribute('data-theme');
    const next = cur === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('ps_theme', next);
    this._update();
  },
  _update() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    document.querySelectorAll('.dark-toggle').forEach(btn => {
      btn.textContent = isDark ? '☀️' : '🌙';
      btn.title = isDark ? 'Switch to light mode' : 'Switch to dark mode';
    });
  }
};
// Auto-init
DarkMode.init();

// ===== PHOTO UPLOAD HELPERS =====
const PhotoUpload = {
  // Call this after profile data loads.
  // avatarEl = element to update (shows img or initials)
  // name = user's name for fallback initials
  // photoUrl = URL from server (null if not set)
  renderAvatar(avatarEl, name, photoUrl) {
    if (!avatarEl) return;
    if (photoUrl) {
      avatarEl.style.backgroundImage = `url(${photoUrl})`;
      avatarEl.style.backgroundSize = 'cover';
      avatarEl.style.backgroundPosition = 'center';
      avatarEl.textContent = '';
    } else {
      avatarEl.style.backgroundImage = '';
      avatarEl.textContent = UI.initials(name);
    }
  },

  // Wire up a photo upload zone
  // opts: { inputId, avatarId, endpoint, onSuccess }
  setup(opts) {
    const input = document.getElementById(opts.inputId);
    if (!input) return;
    input.addEventListener('change', async () => {
      const file = input.files[0]; if (!file) return;
      const allowed = ['image/jpeg','image/png','image/webp','image/gif'];
      if (!allowed.includes(file.type)) { Toast.show('Only JPG, PNG, WebP images allowed','error'); return; }
      if (file.size > 3 * 1024 * 1024) { Toast.show('Image must be under 3MB','error'); return; }
      Toast.show('Uploading photo…', 'info');
      try {
        const fd = new FormData(); fd.append('photo', file);
        const res = await Api.upload(opts.endpoint, fd);
        const avatarEl = document.getElementById(opts.avatarId);
        if (avatarEl && res.photoUrl) {
          PhotoUpload.renderAvatar(avatarEl, Auth.getName(), res.photoUrl);
          // Also update sidebar avatar
          const sav = document.getElementById('s-avatar');
          if (sav) PhotoUpload.renderAvatar(sav, Auth.getName(), res.photoUrl);
        }
        Toast.show('Profile photo updated! 🖼️','success');
        if (opts.onSuccess) opts.onSuccess(res);
      } catch(e) { Toast.show('Photo upload failed: '+e.message,'error'); }
    });
  }
};
