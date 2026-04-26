package com.proyecto.backend.service;

import com.proyecto.backend.model.Producto;
import com.proyecto.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
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
        producto.setCategoria(Producto.Categoria.RELOJ); // ajusta al enum real
    }

    @Test
    void getAllProductos_debeRetornarListaDeProductos() {
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto));

        List<Producto> result = productoService.getAllProductos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Reloj Casio");
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void getProductoById_cuandoExiste_debeRetornarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Optional<Producto> result = productoService.getProductoById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getMarca()).isEqualTo("Casio");
    }

    @Test
    void getProductoById_cuandoNoExiste_debeRetornarVacio() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Producto> result = productoService.getProductoById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createProducto_debeGuardarYRetornarProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto result = productoService.createProducto(producto);

        assertThat(result.getNombre()).isEqualTo("Reloj Casio");
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void updateProducto_cuandoNoExiste_debeLanzarExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.updateProducto(99L, producto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Producto not found");
    }

    @Test
    void deleteProducto_debeInvocarDeleteById() {
        doNothing().when(productoRepository).deleteById(1L);

        productoService.deleteProducto(1L);

        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void searchProductosByNombre_debeRetornarCoincidencias() {
        when(productoRepository.findByNombreContainingIgnoreCase("casio"))
                .thenReturn(Arrays.asList(producto));

        List<Producto> result = productoService.searchProductosByNombre("casio");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).containsIgnoringCase("casio");
    }
}