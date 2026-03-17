(function () {
  const API_URL = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
    ? 'http://localhost:8081/api'
    : 'https://proyecto-x-v1-0-0-backend.onrender.com/api';

  async function fetchProduct(id) {
    try {
      const res = await fetch(`${API_URL}/productos/${id}`);
      return res.ok ? res.json() : null;
    } catch (error) {
      console.error('Error cargando producto:', error);
      return null;
    }
  }

  function getProductIdFromUrl() {
    const path = window.location.pathname;
    const match = path.match(/\/pages\/product\/(\d+)/);
    return match ? parseInt(match[1]) : null;
  }

  function getProductImage(product) {
    // Si el producto tiene imagePath, usarlo; sino, generar por ID
    const basePath = product.imagePath || `/resources/productos/${product.id}`;
    return `${basePath}/Foto1.jpg`;
  }

  function renderProductImage(product) {
    const container = document.createElement('div');
    container.className = 'space-y-3';
    const img = document.createElement('img');
    img.src = getProductImage(product);
    img.alt = product.nombre;
    img.className = 'w-full h-96 object-cover rounded-lg';
    img.onerror = () => { img.src = '/resources/placeholder.jpg'; };
    container.appendChild(img);
    return container;
  }

  document.addEventListener('DOMContentLoaded', async () => {
    const productId = getProductIdFromUrl();
    const root = document.getElementById('product-root');
    
    if (!productId) {
      root.innerHTML = '<div class="p-6 text-center"><p class="text-red-400">ID de producto no válido.</p></div>';
      return;
    }

    root.innerHTML = '<div class="p-6 text-center"><p class="text-smoke/60">Cargando producto...</p></div>';
    
    const product = await fetchProduct(productId);
    
    if (!product) {
      root.innerHTML = '<div class="p-6 text-center"><p class="text-red-400">Producto no encontrado.</p><a href="/" class="text-gold hover:underline mt-4 inline-block">Volver a inicio</a></div>';
      return;
    }

    root.innerHTML = '';
    
    const left = document.createElement('div');
    left.appendChild(renderProductImage(product));

    const right = document.createElement('div');
    right.innerHTML = `
      <div class="space-y-4">
        <h1 class="text-3xl font-semibold text-white">${product.nombre}</h1>
        <p class="text-sm text-gold/80 uppercase tracking-wider">${product.marca || 'GoldenLine'}</p>
        <p class="text-smoke/70 leading-relaxed">${product.descripcion || 'Producto exclusivo de nuestra colección.'}</p>
        <div class="flex items-baseline gap-4 pt-4 border-t border-gold/10">
          <span class="text-gold text-3xl font-semibold">€${product.precio}</span>
          <span class="text-smoke/50 text-sm">Stock: ${product.stock} unidades</span>
        </div>
        <div class="flex gap-3 pt-4">
          <button 
            id="add-to-cart-btn"
            class="flex-1 px-6 py-3 bg-gold text-onyx rounded-full font-semibold uppercase tracking-wider hover:bg-gold/90 transition"
          >
            Añadir al carrito
          </button>
          <a 
            href="/pages/cart" 
            class="px-6 py-3 border border-gold/40 text-gold rounded-full font-semibold uppercase tracking-wider hover:bg-gold/10 transition"
          >
            Ver carrito
          </a>
        </div>
      </div>
    `;

    const addBtn = right.querySelector('#add-to-cart-btn');
    addBtn.onclick = () => {
      if (window.Cart) {
        window.Cart.addItem(product, 1);
        addBtn.textContent = '✓ Añadido';
        addBtn.classList.add('bg-green-600');
        addBtn.classList.remove('bg-gold');
        setTimeout(() => {
          addBtn.textContent = 'Añadir al carrito';
          addBtn.classList.remove('bg-green-600');
          addBtn.classList.add('bg-gold');
        }, 1500);
      } else {
        console.error('Cart no está disponible');
      }
    };

    root.appendChild(left);
    root.appendChild(right);
  });
})();
