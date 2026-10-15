package com.papeleriafundadores.papeleria.service.impl;

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
import com.papeleriafundadores.papeleria.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createFromCart(OrderRequest request) {
        Cart cart = cartRepository.findByCustomer(request.customerId())
                .orElseThrow(() -> new BadRequestException("El cliente no tiene un carrito activo para comprar."));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("El carrito de compras está vacío.");
        }

        // Crear la cabecera del pedido
        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(cart.getCustomer())
                .status(OrderStatus.PENDING)
                .deliveryAddress(request.deliveryAddress())
                .notes(request.notes())
                .subtotal(cart.getTotal())
                .discount(BigDecimal.ZERO)
                .tax(cart.getTotal().multiply(BigDecimal.valueOf(0.19))) // IVA 19% analógico
                .total(cart.getTotal().multiply(BigDecimal.valueOf(1.19)))
                .details(new ArrayList<>())
                .build();

        // Transferir items del carrito a detalles de pedido y descontar inventario
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new InsufficientStockException(product.getName(), product.getStock(), cartItem.getQuantity());
            }

            // Descontar inventario
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .subtotal(cartItem.getSubtotal())
                    .build();
            order.getDetails().add(detail);
        }

        Order savedOrder = orderRepository.save(order);

        // Vaciar el carrito
        cartItemRepository.clearCart(cart.getId());
        cart.setTotal(BigDecimal.ZERO);
        cartRepository.save(cart);

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findByNumber(String orderNumber) {
        return orderRepository.findByNumber(orderNumber)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "número", orderNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomer(Long customerId) {
        return orderRepository.findByCustomer(customerId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        order.setStatus(request.status());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancelOrder(Long id, Long customerId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Este pedido no pertenece al cliente especificado.");
        }

        // Regla de negocio estricta: Solo cancelar si sigue en PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("El pedido ya se encuentra en estado " + order.getStatus() + " y no puede cancelarse.");
        }

        // Devolver stock al inventario
        for (OrderDetail detail : order.getDetails()) {
            Product product = detail.getProduct();
            product.setStock(product.getStock() + detail.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }
}
