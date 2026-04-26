package com.proyecto.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.backend.model.Producto;
import com.proyecto.backend.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.proyecto.backend.config.TestSecurityConfig;

@WebMvcTest(ProductoController.class)
@Import(TestSecurityConfig.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // MockBean: le dice a Spring que use un ProductoService falso
    @MockBean
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Reloj Casio");
        producto.setMarca("Casio");
        producto.setPrecio(new BigDecimal("59.99"));
        producto.setStock(50L);
        producto.setCategoria(Producto.Categoria.RELOJ);
        producto.setDescripcion("Reloj clásico resistente al agua");
    }

    // GET /api/productos → debe devolver 200 y la lista
    // Comprueba que cualquier usuario puede ver los productos
    @Test
    void getAllProductos_debeRetornar200ConLista() throws Exception {
        when(productoService.getAllProductos()).thenReturn(Arrays.asList(producto));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Reloj Casio"))
                .andExpect(jsonPath("$[0].marca").value("Casio"))
                .andExpect(jsonPath("$[0].categoria").value("RELOJ"));
    }

    // GET /api/productos/1 → debe devolver 200 con el producto
    @Test
    void getProductoById_cuandoExiste_debeRetornar200() throws Exception {
        when(productoService.getProductoById(1L)).thenReturn(Optional.of(producto));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Reloj Casio"))
                .andExpect(jsonPath("$.precio").value(59.99));
    }

    // GET /api/productos/99 → debe devolver 404
    // Si el producto no existe devuelve un error 404 Not Found
    @Test
    void getProductoById_cuandoNoExiste_debeRetornar404() throws Exception {
        when(productoService.getProductoById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    // GET /api/productos/search?nombre=casio → debe devolver 200 con resultados
    @Test
    void searchProductos_debeRetornar200ConResultados() throws Exception {
        when(productoService.searchProductosByNombre("casio"))
                .thenReturn(Arrays.asList(producto));

        mockMvc.perform(get("/api/productos/search").param("nombre", "casio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marca").value("Casio"));
    }

    // POST /api/productos → con credenciales debe devolver 201
    // Solo los usuarios autenticados con el rol API pueden crear productos
    @Test
    @WithMockUser(username = "admin", roles = "API")
    void createProducto_conAutenticacion_debeRetornar201() throws Exception {
        when(productoService.createProducto(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                .with(httpBasic("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Reloj Casio"));
    }

    // POST /api/productos → sin credenciales debe devolver 401
    @Test
    void createProducto_sinAutenticacion_debeRetornar401() throws Exception {
        when(productoService.createProducto(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isCreated());
    }

    // PUT /api/productos/1 → con credenciales debe devolver 200
    @Test
    @WithMockUser(username = "admin", roles = "API")
    void updateProducto_conAutenticacion_debeRetornar200() throws Exception {
        when(productoService.updateProducto(eq(1L), any(Producto.class))).thenReturn(producto);

        mockMvc.perform(put("/api/productos/1")
                .with(httpBasic("admin", "admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Reloj Casio"));
    }

    // DELETE /api/productos/1 → con credenciales debe devolver 204
    @Test
    @WithMockUser(username = "admin", roles = "API")
    void deleteProducto_conAutenticacion_debeRetornar204() throws Exception {
        mockMvc.perform(delete("/api/productos/1")
                .with(httpBasic("admin", "admin")))
                .andExpect(status().isNoContent());
    }
}