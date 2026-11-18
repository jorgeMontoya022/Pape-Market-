package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.SaleDetailRequest;
import com.papeleriafundadores.papeleria.dto.request.SaleRequest;
import com.papeleriafundadores.papeleria.dto.response.SaleResponse;
import com.papeleriafundadores.papeleria.entity.*;
import com.papeleriafundadores.papeleria.exception.InsufficientStockException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.SaleMapper;
import com.papeleriafundadores.papeleria.repository.*;
import com.papeleriafundadores.papeleria.service.impl.SaleServiceImpl;
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
 * Pruebas unitarias para SaleServiceImpl.
 *
 * Cubre las reglas de negocio críticas:
 *   - Proveedor inexistente lanza ResourceNotFoundException
 *   - Usuario inexistente lanza ResourceNotFoundException
 *   - Producto inexistente lanza ResourceNotFoundException
 *   - Stock insuficiente lanza InsufficientStockException
 *   - Stock se descuenta al registrar la venta
 *   - Cálculo correcto de subtotal, descuento, IVA y total
 *   - Venta anónima (sin customerId) se registra correctamente
 *   - Precio de venta se congela en el momento de la venta
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaleServiceImpl — Pruebas unitarias")
class SaleServiceImplTest {

    @Mock SaleRepository     saleRepository;
    @Mock CustomerRepository customerRepository;
    @Mock UserRepository     userRepository;
    @Mock ProductRepository  productRepository;
    @Mock SaleMapper         saleMapper;

    @InjectMocks SaleServiceImpl saleService;

    private User    user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Bolígrafo BIC");
        product.setStock(100);
        product.setSalePrice(new BigDecimal("1500"));
    }

    // ---------------------------------------------------------------
    // registerSale — validaciones de existencia
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("registerSale() — validaciones de existencia")
    class RegisterSaleValidaciones {

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el usuario no existe")
        void lanzaExcepcionSiUsuarioNoExiste() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            SaleRequest request = new SaleRequest(null, 1L, BigDecimal.ZERO,
                    List.of(new SaleDetailRequest(10L, 2)));

            assertThatThrownBy(() -> saleService.registerSale(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el producto no existe")
        void lanzaExcepcionSiProductoNoExiste() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            SaleRequest request = new SaleRequest(null, 1L, BigDecimal.ZERO,
                    List.of(new SaleDetailRequest(99L, 2)));

            assertThatThrownBy(() -> saleService.registerSale(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Producto");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el cliente (opcional) no existe")
        void lanzaExcepcionSiClienteNoExiste() {
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            SaleRequest request = new SaleRequest(999L, 1L, BigDecimal.ZERO,
                    List.of(new SaleDetailRequest(10L, 2)));

            assertThatThrownBy(() -> saleService.registerSale(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }
    }

    // ---------------------------------------------------------------
    // registerSale — reglas de inventario
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("registerSale() — reglas de inventario")
    class RegisterSaleInventario {

        @Test
        @DisplayName("Lanza InsufficientStockException si no hay stock suficiente")
        void lanzaExcepcionStockInsuficiente() {
            product.setStock(1); // solo 1, pero piden 5
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));

            SaleRequest request = new SaleRequest(null, 1L, BigDecimal.ZERO,
                    List.of(new SaleDetailRequest(10L, 5)));

            assertThatThrownBy(() -> saleService.registerSale(request))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("Descuenta el stock del producto al registrar la venta")
        void descontaStockAlRegistrarVenta() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
            Sale savedSale = Sale.builder()
                    .id(1L).subtotal(BigDecimal.ZERO).discount(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO).total(BigDecimal.ZERO).details(new ArrayList<>()).build();
            when(saleRepository.save(any())).thenReturn(savedSale);
            when(saleMapper.toResponse(any())).thenReturn(mock(SaleResponse.class));

            SaleRequest request = new SaleRequest(null, 1L, BigDecimal.ZERO,
                    List.of(new SaleDetailRequest(10L, 4)));

            saleService.registerSale(request);

            // Stock original 100 - 4 = 96
            assertThat(product.getStock()).isEqualTo(96);
            verify(productRepository).save(product);
        }
    }

    // ---------------------------------------------------------------
    // registerSale — cálculos financieros
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("registerSale() — cálculos financieros")
    class RegisterSaleCalculos {

        @BeforeEach
        void setupMocks() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        }

        @Test
        @DisplayName("Calcula correctamente subtotal, descuento, IVA y total sin descuento")
        void calculaFinancierosCorrectamenteSinDescuento() {
            Sale[] savedRef = new Sale[1];
            when(saleRepository.save(any())).thenAnswer(inv -> {
                savedRef[0] = inv.getArgument(0);
                return savedRef[0];
            });
            when(saleMapper.toResponse(any())).thenReturn(mock(SaleResponse.class));

            // 10 bolígrafos x $1500 = $15000, sin descuento
            SaleRequest request = new SaleRequest(null, 1L, BigDecimal.ZERO,
                    List.of(new SaleDetailRequest(10L, 10)));

            saleService.registerSale(request);

            // subtotal bruto = 15000
            assertThat(savedRef[0].getSubtotal()).isEqualByComparingTo(new BigDecimal("15000"));
            // descuento = 0
            assertThat(savedRef[0].getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
            // IVA = 15000 * 0.19 = 2850
            assertThat(savedRef[0].getTax()).isEqualByComparingTo(new BigDecimal("2850.00"));
            // total = 15000 + 2850 = 17850
            assertThat(savedRef[0].getTotal()).isEqualByComparingTo(new BigDecimal("17850.00"));
        }

        @Test
        @DisplayName("Aplica descuento porcentual correctamente antes de calcular IVA")
        void aplicaDescuentoAntesDeIva() {
            Sale[] savedRef = new Sale[1];
            when(saleRepository.save(any())).thenAnswer(inv -> {
                savedRef[0] = inv.getArgument(0);
                return savedRef[0];
            });
            when(saleMapper.toResponse(any())).thenReturn(mock(SaleResponse.class));

            // 10 bolígrafos x $1500 = $15000, con 10% de descuento
            SaleRequest request = new SaleRequest(null, 1L, new BigDecimal("10"),
                    List.of(new SaleDetailRequest(10L, 10)));

            saleService.registerSale(request);

            // subtotal bruto = 15000
            assertThat(savedRef[0].getSubtotal()).isEqualByComparingTo(new BigDecimal("15000"));
            // descuento = 15000 * 0.10 = 1500
            assertThat(savedRef[0].getDiscount()).isEqualByComparingTo(new BigDecimal("1500.00"));
            // base después del descuento = 13500
            // IVA = 13500 * 0.19 = 2565
            assertThat(savedRef[0].getTax()).isEqualByComparingTo(new BigDecimal("2565.00"));
            // total = 13500 + 2565 = 16065
            assertThat(savedRef[0].getTotal()).isEqualByComparingTo(new BigDecimal("16065.00"));
        }

        @Test
        @DisplayName("Congela el precio de venta del producto en el momento de la venta")
        void congelaPrecioVentaDelDia() {
            Sale[] savedRef = new Sale[1];
            when(saleRepository.save(any())).thenAnswer(inv -> {
                savedRef[0] = inv.getArgument(0);
                return savedRef[0];
            });
            when(saleMapper.toResponse(any())).thenReturn(mock(SaleResponse.class));

            SaleRequest request = new SaleRequest(null, 1L, BigDecimal.ZERO,
                    List.of(new SaleDetailRequest(10L, 2)));

            saleService.registerSale(request);

            // El detalle debe tener el precio de venta del producto al momento de la transacción
            assertThat(savedRef[0].getDetails()).hasSize(1);
            assertThat(savedRef[0].getDetails().get(0).getSalePrice())
                    .isEqualByComparingTo(new BigDecimal("1500"));
        }

        @Test
        @DisplayName("Registra venta anónima (sin customerId) correctamente")
        void registraVentaAnonimaSinCliente() {
            Sale savedSale = Sale.builder()
                    .id(1L).subtotal(BigDecimal.ZERO).discount(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO).total(BigDecimal.ZERO).details(new ArrayList<>()).build();
            when(saleRepository.save(any())).thenReturn(savedSale);
            when(saleMapper.toResponse(any())).thenReturn(mock(SaleResponse.class));

            // Sin customerId — venta de mostrador anónima
            SaleRequest request = new SaleRequest(null, 1L, BigDecimal.ZERO,
                    List.of(new SaleDetailRequest(10L, 1)));

            assertThatCode(() -> saleService.registerSale(request))
                    .doesNotThrowAnyException();

            // No se debe llamar a customerRepository si customerId es null
            verify(customerRepository, never()).findById(any());
        }
    }
}
