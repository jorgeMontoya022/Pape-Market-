package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.OrderRequest;
import com.papeleriafundadores.papeleria.dto.request.OrderStatusUpdateRequest;
import com.papeleriafundadores.papeleria.dto.response.OrderResponse;
import com.papeleriafundadores.papeleria.entity.*;
import com.papeleriafundadores.papeleria.entity.enums.OrderStatus;
import com.papeleriafundadores.papeleria.exception.BadRequestException;
import com.papeleriafundadores.papeleria.exception.InsufficientStockException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.OrderMapper;
import com.papeleriafundadores.papeleria.repository.*;
import com.papeleriafundadores.papeleria.service.impl.OrderServiceImpl;
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
 * Pruebas unitarias para OrderServiceImpl.
 *
 * Cubre las reglas de negocio críticas:
 *   - Carrito vacío no puede generar pedido
 *   - Stock insuficiente bloquea la creación del pedido
 *   - Stock se descuenta al crear el pedido
 *   - Carrito se vacía tras confirmar el pedido
 *   - Solo pedidos PENDING pueden cancelarse
 *   - Cancelar devuelve el stock al inventario
 *   - Cancelar con customerId incorrecto lanza error
 *   - updateStatus cambia el estado correctamente
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl — Pruebas unitarias")
class OrderServiceImplTest {

    @Mock OrderRepository     orderRepository;
    @Mock CartRepository      cartRepository;
    @Mock CartItemRepository  cartItemRepository;
    @Mock ProductRepository   productRepository;
    @Mock OrderMapper         orderMapper;

    @InjectMocks OrderServiceImpl orderService;

    // ---------------------------------------------------------------
    // Fixtures reutilizables
    // ---------------------------------------------------------------

    private Customer customer;
    private Product  product;
    private Cart     cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Cuaderno Norma");
        product.setStock(50);
        product.setSalePrice(new BigDecimal("7500"));

        cartItem = CartItem.builder()
                .id(1L)
                .quantity(3)
                .unitPrice(new BigDecimal("7500"))
                .subtotal(new BigDecimal("22500"))
                .product(product)
                .build();

        cart = Cart.builder()
                .id(1L)
                .customer(customer)
                .total(new BigDecimal("22500"))
                .items(new ArrayList<>(List.of(cartItem)))
                .build();
    }

    // ---------------------------------------------------------------
    // createFromCart
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("createFromCart()")
    class CreateFromCart {

        @Test
        @DisplayName("Lanza BadRequestException si el cliente no tiene carrito")
        void lanzaExcepcionSiNoHayCarrito() {
            when(cartRepository.findByCustomer(1L)).thenReturn(Optional.empty());

            OrderRequest request = new OrderRequest("Calle 1", null, 1L);

            assertThatThrownBy(() -> orderService.createFromCart(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("carrito");
        }

        @Test
        @DisplayName("Lanza BadRequestException si el carrito está vacío")
        void lanzaExcepcionSiCarritoVacio() {
            cart.setItems(new ArrayList<>());
            when(cartRepository.findByCustomer(1L)).thenReturn(Optional.of(cart));

            OrderRequest request = new OrderRequest("Calle 1", null, 1L);

            assertThatThrownBy(() -> orderService.createFromCart(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("vacío");
        }

        @Test
        @DisplayName("Lanza InsufficientStockException si no hay stock suficiente")
        void lanzaExcepcionStockInsuficiente() {
            product.setStock(1); // solo 1 en stock, el carrito pide 3
            when(cartRepository.findByCustomer(1L)).thenReturn(Optional.of(cart));

            OrderRequest request = new OrderRequest("Calle 1", null, 1L);

            assertThatThrownBy(() -> orderService.createFromCart(request))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("Descuenta el stock del producto al crear el pedido")
        void descontaStockAlCrearPedido() {
            when(cartRepository.findByCustomer(1L)).thenReturn(Optional.of(cart));
            Order savedOrder = Order.builder()
                    .id(1L).orderNumber("ORD-ABC123")
                    .status(OrderStatus.PENDING)
                    .subtotal(new BigDecimal("22500"))
                    .discount(BigDecimal.ZERO)
                    .tax(new BigDecimal("4275"))
                    .total(new BigDecimal("26775"))
                    .customer(customer)
                    .details(new ArrayList<>())
                    .build();
            when(orderRepository.save(any())).thenReturn(savedOrder);
            when(orderMapper.toResponse(any())).thenReturn(mock(OrderResponse.class));

            orderService.createFromCart(new OrderRequest("Calle 1", null, 1L));

            // Stock original 50 - 3 (cantidad del carrito) = 47
            assertThat(product.getStock()).isEqualTo(47);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Vacía el carrito tras confirmar el pedido")
        void vaciCarritoTrasCrearPedido() {
            when(cartRepository.findByCustomer(1L)).thenReturn(Optional.of(cart));
            Order savedOrder = Order.builder()
                    .id(1L).orderNumber("ORD-XYZ")
                    .status(OrderStatus.PENDING)
                    .subtotal(new BigDecimal("22500"))
                    .discount(BigDecimal.ZERO)
                    .tax(new BigDecimal("4275"))
                    .total(new BigDecimal("26775"))
                    .customer(customer)
                    .details(new ArrayList<>())
                    .build();
            when(orderRepository.save(any())).thenReturn(savedOrder);
            when(orderMapper.toResponse(any())).thenReturn(mock(OrderResponse.class));

            orderService.createFromCart(new OrderRequest("Calle 1", null, 1L));

            verify(cartItemRepository).clearCart(cart.getId());
            assertThat(cart.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("Calcula IVA del 19% sobre el total del carrito")
        void calculaIvaCorrectamente() {
            when(cartRepository.findByCustomer(1L)).thenReturn(Optional.of(cart));

            Order[] savedOrderRef = new Order[1];
            when(orderRepository.save(any())).thenAnswer(inv -> {
                savedOrderRef[0] = inv.getArgument(0);
                return savedOrderRef[0];
            });
            when(orderMapper.toResponse(any())).thenReturn(mock(OrderResponse.class));

            orderService.createFromCart(new OrderRequest("Calle 1", null, 1L));

            // subtotal = 22500, IVA = 22500 * 0.19 = 4275, total = 22500 * 1.19 = 26775
            assertThat(savedOrderRef[0].getTax())
                    .isEqualByComparingTo(new BigDecimal("4275.00"));
            assertThat(savedOrderRef[0].getTotal())
                    .isEqualByComparingTo(new BigDecimal("26775.00"));
        }
    }

    // ---------------------------------------------------------------
    // cancelOrder
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        private Order pendingOrder;

        @BeforeEach
        void setUpOrder() {
            OrderDetail detail = OrderDetail.builder()
                    .id(1L).quantity(3).product(product)
                    .unitPrice(new BigDecimal("7500"))
                    .subtotal(new BigDecimal("22500"))
                    .build();

            pendingOrder = Order.builder()
                    .id(1L)
                    .orderNumber("ORD-TEST01")
                    .status(OrderStatus.PENDING)
                    .customer(customer)
                    .subtotal(new BigDecimal("22500"))
                    .discount(BigDecimal.ZERO)
                    .tax(new BigDecimal("4275"))
                    .total(new BigDecimal("26775"))
                    .details(new ArrayList<>(List.of(detail)))
                    .build();
        }

        @Test
        @DisplayName("Cancela un pedido PENDING y devuelve el stock")
        void cancelaPedidoPendingYDevuelveStock() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            orderService.cancelOrder(1L, 1L);

            assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
            // Stock 50 + 3 (devueltos) = 53
            assertThat(product.getStock()).isEqualTo(53);
            verify(productRepository).save(product);
            verify(orderRepository).save(pendingOrder);
        }

        @Test
        @DisplayName("Lanza BadRequestException si el pedido no está en PENDING")
        void lanzaExcepcionSiPedidoNoPending() {
            pendingOrder.setStatus(OrderStatus.PAID);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PAID");
        }

        @Test
        @DisplayName("Lanza BadRequestException si el customerId no coincide")
        void lanzaExcepcionSiClienteNoCoincide() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 999L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("no pertenece");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el pedido no existe")
        void lanzaExcepcionSiPedidoNoExiste() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(99L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ---------------------------------------------------------------
    // updateStatus
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Actualiza el estado del pedido correctamente")
        void actualizaEstadoCorrectamente() {
            Order order = Order.builder()
                    .id(1L).status(OrderStatus.PENDING)
                    .customer(customer)
                    .subtotal(BigDecimal.ZERO).discount(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO).total(BigDecimal.ZERO)
                    .details(new ArrayList<>()).build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);
            when(orderMapper.toResponse(any())).thenReturn(mock(OrderResponse.class));

            orderService.updateStatus(1L, new OrderStatusUpdateRequest(OrderStatus.PAID));

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el pedido no existe")
        void lanzaExcepcionSiPedidoNoExiste() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    orderService.updateStatus(99L, new OrderStatusUpdateRequest(OrderStatus.PAID)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
