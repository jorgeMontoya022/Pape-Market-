package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.PurchaseDetailRequest;
import com.papeleriafundadores.papeleria.dto.request.PurchaseRequest;
import com.papeleriafundadores.papeleria.dto.response.PurchaseResponse;
import com.papeleriafundadores.papeleria.entity.*;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.PurchaseMapper;
import com.papeleriafundadores.papeleria.repository.*;
import com.papeleriafundadores.papeleria.service.impl.PurchaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PurchaseServiceImpl.
 *
 * Cubre las reglas de negocio críticas:
 *   - Proveedor inexistente lanza ResourceNotFoundException
 *   - Usuario inexistente lanza ResourceNotFoundException
 *   - Producto inexistente lanza ResourceNotFoundException
 *   - Stock se incrementa al registrar la compra
 *   - Precio de costo histórico se actualiza con el valor de la compra
 *   - Total de la compra se calcula correctamente (cantidad × costo)
 *   - Múltiples productos en una sola compra se procesan correctamente
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseServiceImpl — Pruebas unitarias")
class PurchaseServiceImplTest {

    @Mock PurchaseRepository purchaseRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock UserRepository     userRepository;
    @Mock ProductRepository  productRepository;
    @Mock PurchaseMapper     purchaseMapper;

    @InjectMocks PurchaseServiceImpl purchaseService;

    private Supplier supplier;
    private User     user;
    private Product  product;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setCompanyName("Distribuidora Norma");

        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Cuaderno 100H");
        product.setStock(50);
        product.setPurchasePrice(new BigDecimal("3500"));
    }

    // ---------------------------------------------------------------
    // registerPurchase — validaciones de existencia
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("registerPurchase() — validaciones de existencia")
    class RegisterPurchaseValidaciones {

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el proveedor no existe")
        void lanzaExcepcionSiProveedorNoExiste() {
            when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

            PurchaseRequest request = new PurchaseRequest(99L, 1L,
                    List.of(new PurchaseDetailRequest(10L, 5, new BigDecimal("3500"))));

            assertThatThrownBy(() -> purchaseService.registerPurchase(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Proveedor");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el usuario no existe")
        void lanzaExcepcionSiUsuarioNoExiste() {
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            PurchaseRequest request = new PurchaseRequest(1L, 99L,
                    List.of(new PurchaseDetailRequest(10L, 5, new BigDecimal("3500"))));

            assertThatThrownBy(() -> purchaseService.registerPurchase(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el producto no existe")
        void lanzaExcepcionSiProductoNoExiste() {
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            PurchaseRequest request = new PurchaseRequest(1L, 1L,
                    List.of(new PurchaseDetailRequest(99L, 5, new BigDecimal("3500"))));

            assertThatThrownBy(() -> purchaseService.registerPurchase(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Producto");
        }
    }

    // ---------------------------------------------------------------
    // registerPurchase — reglas de inventario
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("registerPurchase() — reglas de inventario")
    class RegisterPurchaseInventario {

        @BeforeEach
        void setupMocks() {
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        }

        @Test
        @DisplayName("Incrementa el stock del producto al registrar la compra")
        void incrementaStockAlRegistrarCompra() {
            Purchase savedPurchase = Purchase.builder()
                    .id(1L).total(new BigDecimal("17500"))
                    .details(new ArrayList<>()).build();
            when(purchaseRepository.save(any())).thenReturn(savedPurchase);
            when(purchaseMapper.toResponse(any())).thenReturn(mock(PurchaseResponse.class));

            PurchaseRequest request = new PurchaseRequest(1L, 1L,
                    List.of(new PurchaseDetailRequest(10L, 20, new BigDecimal("3500"))));

            purchaseService.registerPurchase(request);

            // Stock original 50 + 20 = 70
            assertThat(product.getStock()).isEqualTo(70);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Actualiza el precio de costo histórico del producto")
        void actualizaPrecioCostoHistorico() {
            Purchase savedPurchase = Purchase.builder()
                    .id(1L).total(new BigDecimal("80000"))
                    .details(new ArrayList<>()).build();
            when(purchaseRepository.save(any())).thenReturn(savedPurchase);
            when(purchaseMapper.toResponse(any())).thenReturn(mock(PurchaseResponse.class));

            BigDecimal nuevoCosto = new BigDecimal("4000");
            PurchaseRequest request = new PurchaseRequest(1L, 1L,
                    List.of(new PurchaseDetailRequest(10L, 20, nuevoCosto)));

            purchaseService.registerPurchase(request);

            // El precio de costo del producto debe actualizarse con el nuevo valor
            assertThat(product.getPurchasePrice()).isEqualByComparingTo(nuevoCosto);
        }
    }

    // ---------------------------------------------------------------
    // registerPurchase — cálculos financieros
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("registerPurchase() — cálculos financieros")
    class RegisterPurchaseCalculos {

        @BeforeEach
        void setupMocks() {
            when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        }

        @Test
        @DisplayName("Calcula el total correctamente para un solo producto")
        void calculaTotalUnProducto() {
            Purchase[] savedRef = new Purchase[1];
            when(purchaseRepository.save(any())).thenAnswer(inv -> {
                savedRef[0] = inv.getArgument(0);
                return savedRef[0];
            });
            when(purchaseMapper.toResponse(any())).thenReturn(mock(PurchaseResponse.class));

            // 20 unidades x $3500 = $70000
            PurchaseRequest request = new PurchaseRequest(1L, 1L,
                    List.of(new PurchaseDetailRequest(10L, 20, new BigDecimal("3500"))));

            purchaseService.registerPurchase(request);

            assertThat(savedRef[0].getTotal()).isEqualByComparingTo(new BigDecimal("70000.00"));
        }

        @Test
        @DisplayName("Calcula el total correctamente para múltiples productos")
        void calculaTotalMultiplesProductos() {
            Product product2 = new Product();
            product2.setId(20L);
            product2.setName("Bolígrafo BIC");
            product2.setStock(200);
            product2.setPurchasePrice(new BigDecimal("800"));

            when(productRepository.findById(20L)).thenReturn(Optional.of(product2));

            Purchase[] savedRef = new Purchase[1];
            when(purchaseRepository.save(any())).thenAnswer(inv -> {
                savedRef[0] = inv.getArgument(0);
                return savedRef[0];
            });
            when(purchaseMapper.toResponse(any())).thenReturn(mock(PurchaseResponse.class));

            // Producto 1: 10 x $3500 = $35000
            // Producto 2: 50 x $800  = $40000
            // Total = $75000
            PurchaseRequest request = new PurchaseRequest(1L, 1L, List.of(
                    new PurchaseDetailRequest(10L, 10, new BigDecimal("3500")),
                    new PurchaseDetailRequest(20L, 50, new BigDecimal("800"))
            ));

            purchaseService.registerPurchase(request);

            assertThat(savedRef[0].getTotal()).isEqualByComparingTo(new BigDecimal("75000.00"));
            assertThat(savedRef[0].getDetails()).hasSize(2);
        }
    }
}
