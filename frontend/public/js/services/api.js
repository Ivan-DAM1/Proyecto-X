function resolveApiBaseUrl() {
    const prodUrl = 'https://proyecto-x-v1-0-0-backend.onrender.com/api';
    const host = window.location.hostname;
    if (host === 'localhost' || host === '127.0.0.1') {
        return 'http://localhost:8081/api';
    }
    return prodUrl;
}

// Configuración de la API, a futuro en el .env
const API_CONFIG = {
    BASE_URL: resolveApiBaseUrl(),
    ENDPOINTS: {
        PRODUCTOS: '/productos',
        USUARIOS: '/users',
        PEDIDOS: '/pedidos',
        HEALTH: '/health'
    }
};

// Servicio de API
class ApiService {
    constructor(baseUrl = API_CONFIG.BASE_URL) {
        this.baseUrl = baseUrl;
    }

    /*
     * EJEMPLO - Crear producto con el cambio de Autenticación
     * const username = 'admin';
     * const password = 'admin';
     * const authHeader = 'Basic ' + btoa(`${username}:${password}`);
     *
     * const nuevoProducto = {
     *   nombre: 'Prueba',
     *   descripcion: 'Este es un producto de prueba bleh',
     *   precio: 10000.0,
     *   stock: 123,
     *   marca: 'RiberaDelTajo',
     *   categoria: 'ACCESORIOS'
     * };
     *
     * await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.PRODUCTOS}`, {
     *   method: 'POST',
     *   headers: {
     *     'Content-Type': 'application/json',
     *     'Authorization': authHeader
     *   },
     *   body: JSON.stringify(nuevoProducto)
     * });
     */

    async fetchJson(endpoint, options = {}) {
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error(`Error fetching ${endpoint}:`, error);
            throw error;
        }
    }

    // Productos
    async getProductos() {
        return this.fetchJson(API_CONFIG.ENDPOINTS.PRODUCTOS);
    }

    async getProducto(id) {
        return this.fetchJson(`${API_CONFIG.ENDPOINTS.PRODUCTOS}/${id}`);
    }

    async getProductosByCategoria(categoria) {
        return this.fetchJson(`${API_CONFIG.ENDPOINTS.PRODUCTOS}/categoria/${categoria}`);
    }

    async searchProductos(nombre) {
        return this.fetchJson(`${API_CONFIG.ENDPOINTS.PRODUCTOS}/search?nombre=${encodeURIComponent(nombre)}`);
    }

    // Pedidos
    async createPedido(pedido) {
        return this.fetchJson(API_CONFIG.ENDPOINTS.PEDIDOS, {
            method: 'POST',
            body: JSON.stringify(pedido)
        });
    }

    // Health check
    async checkHealth() {
        return this.fetchJson(API_CONFIG.ENDPOINTS.HEALTH);
    }
}

// Instancia global del servicio
const apiService = new ApiService();
