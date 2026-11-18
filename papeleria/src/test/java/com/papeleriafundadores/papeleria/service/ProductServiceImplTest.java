package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.ProductRequest;
import com.papeleriafundadores.papeleria.dto.response.ProductResponse;
import com.papeleriafundadores.papeleria.entity.Category;
import com.papeleriafundadores.papeleria.entity.Product;
import com.papeleriafundadores.papeleria.entity.Supplier;
import com.papeleriafundadores.papeleria.exception.BadRequestException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.ProductMapper;
import com.papeleriafundadores.papeleria.repository.CategoryRepository;
import com.papeleriafundadores.papeleria.repository.ProductRepository;
import com.papeleriafundadores.papeleria.repository.SupplierRepository;
import com.papeleriafundadores.papeleria.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ProductServiceImpl.
 *
 * Cubre las reglas de negocio críticas:
 *   - Código de barras duplicado lanza BadRequestException al crear
 *   - Categoría inexistente lanza ResourceNotFoundException
 *   - Proveedor inexistente lanza ResourceNotFoundException
 *   - Producto creado con categoría y proveedor correctos
 *   - toggleStatus invierte el estado activo/inactivo del producto
 *   - findById lanza ResourceNotFoundException si no existe
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl — Pruebas unitarias")
class ProductServiceImplTest {

    @Mock ProductRepository  productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock ProductMapper      productMapper;

    @InjectMocks ProductServiceImpl productService;

    private Category category;
    private Supplier supplier;
    private ProductRequest validRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Cuadernos");

        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setCompanyName("Distribuidora Norma");

        validRequest = new ProductRequest(
                "Cuaderno 100H", "Cuaderno rayado", "7701234567",
                "Norma", new BigDecimal("3500"), new BigDecimal("6000"),
                50, 5, null, 1L, 1L
        );
    }

    // ---------------------------------------------------------------
    // create
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Lanza BadRequestException si el código de barras ya existe")
        void lanzaExcepcionCodigoBarrasDuplicado() {
            when(productRepository.findByBarcode("7701234567"))
                    .thenReturn(Optional.of(new Product()));

            assertThatThrownBy(() -> productService.create(validRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("7701234567");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si la categoría no existe")
        void lanzaExcepcionCategoriaNoExiste() {
            when(productRepository.findByBarcode(any())).thenReturn(Optional.empty());
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Categoría");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el proveedor no existe")
        void lanzaExcepcionProveedorNoExiste() {
            when(productRepository.findByBarcode(any())).thenReturn(Optional.empty());
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Proveedor");
        }

        @Test
        @DisplayName("Crea el producto con categoría y proveedor asignados correctamente")
        void creaProductoCorrectamente() {
            when(productRepository.findByBarcode(any())).thenReturn(Optional.empty());
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

            Product product = new Product();
            when(productMapper.toEntity(validRequest)).thenReturn(product);
            when(productRepository.save(any())).thenReturn(product);
            when(productMapper.toResponse(any())).thenReturn(mock(ProductResponse.class));

            productService.create(validRequest);

            assertThat(product.getCategory()).isEqualTo(category);
            assertThat(product.getSupplier()).isEqualTo(supplier);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Permite crear producto sin código de barras (barcode null)")
        void permiteCrearProductoSinCodigoBarras() {
            ProductRequest sinBarcode = new ProductRequest(
                    "Cuaderno sin código", "Descripción", null,
                    "Norma", new BigDecimal("3500"), new BigDecimal("6000"),
                    50, 5, null, 1L, 1L
            );
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
            Product product = new Product();
            when(productMapper.toEntity(sinBarcode)).thenReturn(product);
            when(productRepository.save(any())).thenReturn(product);
            when(productMapper.toResponse(any())).thenReturn(mock(ProductResponse.class));

            assertThatCode(() -> productService.create(sinBarcode))
                    .doesNotThrowAnyException();

            // No debe consultar por código de barras si es null
            verify(productRepository, never()).findByBarcode(any());
        }
    }

    // ---------------------------------------------------------------
    // toggleStatus
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("toggleStatus()")
    class ToggleStatus {

        @Test
        @DisplayName("Cambia producto activo a inactivo")
        void cambiaActivoAInactivo() {
            Product product = new Product();
            product.setId(1L);
            product.setStatus(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            productService.toggleStatus(1L);

            assertThat(product.getStatus()).isFalse();
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Cambia producto inactivo a activo")
        void cambiaInactivoAActivo() {
            Product product = new Product();
            product.setId(1L);
            product.setStatus(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            productService.toggleStatus(1L);

            assertThat(product.getStatus()).isTrue();
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el producto no existe")
        void lanzaExcepcionSiProductoNoExiste() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.toggleStatus(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ---------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el producto no existe")
        void lanzaExcepcionSiNoExiste() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Producto");
        }

        @Test
        @DisplayName("Devuelve el producto si existe")
        void devuelveProductoSiExiste() {
            Product product = new Product();
            product.setId(1L);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productMapper.toResponse(product)).thenReturn(mock(ProductResponse.class));

            assertThatCode(() -> productService.findById(1L))
                    .doesNotThrowAnyException();
        }
    }
}
