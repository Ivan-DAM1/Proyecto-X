// Componente reutilizable: SiteFooter
// Uso:
//   <div id="site-footer"></div>
//   <script src="/js/components/site-footer.js"></script>
//   <script>window.SiteFooter.mount({ variant: 'home' });</script>

(function () {
  const LOGO_SRC = '/resources/logo.svg';

  class SiteFooter {
    constructor(options = {}) {
      this.options = options;
      this.variant = options.variant || 'simple';
    }

    render() {
      switch (this.variant) {
        case 'home':
          return this.renderHome();
        case 'status':
          return this.renderStatus();
        case 'simple':
        default:
          return this.renderSimple();
      }
    }

    brandMarkup({ sizeClass } = {}) {
      const imgClass = sizeClass || 'h-6 w-auto';
      return `
        <a href="/" class="inline-flex items-center" aria-label="GoldenLine">
          <img src="${LOGO_SRC}" alt="GoldenLine" class="${imgClass}">
        </a>
      `.trim();
    }

    renderHome() {
      const footer = document.createElement('footer');
      footer.className = 'bg-graphite border-t border-gold/10';

      footer.innerHTML = `
        <div class="max-w-7xl mx-auto px-5 py-10 grid gap-10 md:grid-cols-3">
          <div class="space-y-3">
            ${this.brandMarkup({ sizeClass: 'h-7 w-auto' })}
            <p class="text-sm text-smoke/60">Curaduría de lujo en tonos monocromáticos y dorados. Diseños atemporales con materiales premium.</p>
          </div>
          <div class="space-y-2 text-sm text-smoke/60">
            <p class="text-xs uppercase tracking-[0.4em] text-gold/70">Navegación</p>
            <a href="#destacados" class="block hover:text-gold transition">Destacados</a>
            <a href="#colecciones" class="block hover:text-gold transition">Experiencia</a>
            <a href="/pages/cart" class="block hover:text-gold transition">Carrito</a>
          </div>
          <div class="space-y-2 text-sm text-smoke/60">
            <p class="text-xs uppercase tracking-[0.4em] text-gold/70">Soporte</p>
            <a href="/pages/contact" class="block hover:text-gold transition">Página de contacto</a>
            <a href="mailto:concierge@goldenline.com" class="block hover:text-gold transition">concierge@goldenline.com</a>
            <p class="text-smoke/50">Atención telefónica: +34 000 000 000</p>
          </div>
        </div>
        <div class="border-t border-gold/10 py-4 text-center text-xs text-smoke/40 uppercase tracking-[0.4em]">© 2025 GoldenLine. Todos los derechos reservados.</div>
      `;

      return footer;
    }

    renderSimple() {
      const footer = document.createElement('footer');
      footer.className = 'bg-graphite border-t border-gold/10';

      const links = Array.isArray(this.options.links) ? this.options.links : [
        { href: '/pages/contact', label: 'Contacto' },
        { href: '/pages/status', label: 'Status' },
      ];

      footer.innerHTML = `
        <div class="max-w-7xl mx-auto px-5 py-8 flex flex-col md:flex-row gap-4 md:items-center md:justify-between">
          <div class="text-xs text-smoke/50 uppercase tracking-[0.4em]">© 2025 GoldenLine</div>
          <div class="text-xs uppercase tracking-[0.3em] text-smoke/60 flex gap-5">
            ${links
              .map((l) => `<a href="${l.href}" class="hover:text-gold transition">${l.label}</a>`)
              .join('')}
          </div>
        </div>
      `;

      return footer;
    }

    renderStatus() {
      const footer = document.createElement('footer');
      footer.className = 'bg-charcoal text-slate py-6';

      footer.innerHTML = `
        <div class="max-w-6xl mx-auto px-4 text-sm text-slate-200">
          <p>© 2025 GoldenLine</p>
          <p class="mt-2">Sistema: SQLite</p>
        </div>
      `;

      return footer;
    }
  }

  function mount(options = {}) {
    const targetId = options.targetId || 'site-footer';
    const target = document.getElementById(targetId);
    if (!target) return;

    const footer = new SiteFooter(options);
    target.replaceWith(footer.render());
  }

  window.SiteFooter = { mount, SiteFooter };
})();
