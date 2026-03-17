// Componente reutilizable: SiteHeader
// Uso:
//   <div id="site-header"></div>
//   <script src="/js/components/site-header.js"></script>
//   <script>window.SiteHeader.mount({ variant: 'home' });</script>

(function () {
  const LOGO_SRC = '/resources/logo.svg';

  function escapeHtml(value) {
    const str = String(value ?? '');
    return str
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function readAuthUser() {
    try {
      const raw = localStorage.getItem('authUser');
      if (!raw) return null;
      const parsed = JSON.parse(raw);
      if (!parsed || typeof parsed !== 'object') return null;
      if (!parsed.nombreCompleto && !parsed.email) return null;
      return parsed;
    } catch {
      return null;
    }
  }

  class SiteHeader {
    constructor(options = {}) {
      this.options = options;
      this.variant = options.variant || 'simple';
      this.authUser = readAuthUser();
    }

    authGreetingMarkup({ compact = false } = {}) {
      if (!this.authUser) return '';

      const label = escapeHtml(this.authUser.nombreCompleto || this.authUser.email);
      const className = compact
        ? 'text-xs uppercase tracking-[0.3em] text-smoke/70'
        : 'text-xs uppercase tracking-widest text-smoke/70';

      return `<span class="${className}"><span class="text-smoke">${label}</span></span>`;
    }

    authControlsMarkup({ compact = false } = {}) {
      if (!this.authUser) return '';

      const logoutClass = compact
        ? 'text-xs uppercase tracking-[0.3em] hover:text-gold transition'
        : 'px-4 py-2 border border-gold/40 rounded-full text-sm uppercase tracking-widest hover:border-gold hover:text-gold transition';

      const wrapperClass = compact ? 'inline-flex items-center gap-4' : 'inline-flex items-center gap-4';

      return `
        <span class="${wrapperClass}">
          ${this.authGreetingMarkup({ compact })}
          <button type="button" data-action="logout" class="${logoutClass}">Logout</button>
        </span>
      `.trim();
    }

    render() {
      switch (this.variant) {
        case 'home':
          return this.renderHome();
        case 'auth':
          return this.renderAuth();
        case 'status':
          return this.renderStatus();
        case 'simple':
        default:
          return this.renderSimple();
      }
    }

    brandMarkup({ sizeClass, textClass } = {}) {
      const imgClass = sizeClass || 'h-10 w-auto';
      const wrapperClass = textClass || 'inline-flex items-center';

      return `
        <a href="/" class="${wrapperClass}" aria-label="GoldenLine">
          <img src="${LOGO_SRC}" alt="GoldenLine" class="${imgClass}">
        </a>
      `.trim();
    }

    renderHome() {
      const nav = document.createElement('nav');
      nav.className = 'fixed inset-x-0 top-0 z-20 bg-graphite/90 backdrop-blur border-b border-gold/20 shadow-[0_10px_30px_-20px_rgba(0,0,0,0.8)]';

      const authArea = this.authUser
        ? this.authControlsMarkup()
        : `
              <a href="/pages/login" class="px-4 py-2 border border-gold/40 rounded-full text-sm uppercase tracking-widest hover:border-gold hover:text-gold transition">Login</a>
              <a href="/pages/register" class="px-4 py-2 bg-gold text-onyx rounded-full text-sm uppercase tracking-widest font-semibold hover:bg-gold/90 transition">Register</a>
            `.trim();

      nav.innerHTML = `
        <div class="max-w-7xl mx-auto px-5">
          <div class="flex items-center justify-between h-16">
            ${this.brandMarkup({ sizeClass: 'h-10 w-auto' })}
            <div class="hidden md:flex items-center gap-8 text-sm uppercase tracking-widest">
              <a href="#colecciones" class="hover:text-gold transition">Colecciones</a>
              <a href="#destacados" class="hover:text-gold transition">Destacados</a>
              <a href="/pages/contact" class="hover:text-gold transition">Contacto</a>
            </div>
            <div class="flex items-center gap-4">
              <a href="/pages/cart" class="relative inline-flex items-center" aria-label="Carrito">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="text-smoke/80">
                  <path d="M6 6h15l-1.5 9h-11L6 6z" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"></path>
                  <circle cx="10" cy="20" r="1" fill="currentColor"></circle>
                  <circle cx="18" cy="20" r="1" fill="currentColor"></circle>
                </svg>
                <span id="cart-count" class="absolute -top-2 -right-2 bg-gold text-onyx rounded-full text-xs w-5 h-5 flex items-center justify-center" style="display:none"></span>
              </a>
              ${authArea}
            </div>
          </div>
        </div>
      `;

      return nav;
    }

    renderSimple() {
      const nav = document.createElement('nav');
      nav.className = 'fixed inset-x-0 top-0 z-20 bg-graphite/90 backdrop-blur border-b border-gold/20';

      const links = Array.isArray(this.options.links) ? this.options.links : [
        { href: '/', label: 'Inicio' },
        { href: '/pages/cart', label: 'Carrito' },
        { href: '/pages/contact', label: 'Contacto' },
      ];

      nav.innerHTML = `
        <div class="max-w-7xl mx-auto px-5">
          <div class="flex items-center justify-between h-16">
            ${this.brandMarkup({ sizeClass: 'h-10 w-auto' })}
            <div class="flex items-center gap-4 text-xs uppercase tracking-[0.3em]">
              ${links
                .map((l) => `<a href="${l.href}" class="hover:text-gold transition">${l.label}</a>`)
                .join('')}
              ${this.authUser ? this.authControlsMarkup({ compact: true }) : ''}
            </div>
          </div>
        </div>
      `;

      return nav;
    }

    renderAuth() {
      const nav = document.createElement('nav');
      nav.className = 'bg-graphite/95 border-b border-gold/20';

      const links = Array.isArray(this.options.links) ? this.options.links : [
        { href: '/', label: 'Inicio' },
      ];

      nav.innerHTML = `
        <div class="max-w-4xl mx-auto px-5 h-16 flex items-center justify-between">
          ${this.brandMarkup({ sizeClass: 'h-9 w-auto' })}
          <div class="flex items-center gap-4 text-xs uppercase tracking-[0.3em]">
            ${links
              .map((l) => `<a href="${l.href}" class="hover:text-gold transition">${l.label}</a>`)
              .join('')}
            ${this.authUser ? this.authControlsMarkup({ compact: true }) : ''}
          </div>
        </div>
      `;

      return nav;
    }

    renderStatus() {
      const nav = document.createElement('nav');
      nav.className = 'bg-charcoal text-white';

      const authArea = this.authUser
        ? this.authControlsMarkup({ compact: true })
        : `
            <a href="/pages/login" class="hover:text-gold transition">Login</a>
            <a href="/pages/register" class="hover:text-gold transition">Register</a>
          `.trim();

      nav.innerHTML = `
        <div class="max-w-6xl mx-auto px-4 py-4 flex items-center justify-between">
          ${this.brandMarkup({ sizeClass: 'h-10 w-auto', textClass: 'inline-flex items-center' })}
          <div class="flex items-center gap-6 text-sm uppercase">
            <a href="/" class="hover:text-gold transition">Inicio</a>
            ${authArea}
            <a href="/pages/status" class="text-gold font-semibold">Status</a>
          </div>
        </div>
      `;

      return nav;
    }
  }

  function wireActions(rootEl) {
    if (!rootEl) return;

    const logoutBtn = rootEl.querySelector('[data-action="logout"]');
    if (!logoutBtn) return;

    logoutBtn.addEventListener('click', () => {
      localStorage.removeItem('authUser');
      window.location.reload();
    });
  }

  function mount(options = {}) {
    const targetId = options.targetId || 'site-header';
    const target = document.getElementById(targetId);
    if (!target) return;

    const header = new SiteHeader(options);
    const rendered = header.render();
    target.replaceWith(rendered);
    wireActions(rendered);
  }

  window.SiteHeader = { mount, SiteHeader };
})();
