package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.OrderRequest;
import com.papeleriafundadores.papeleria.dto.request.OrderStatusUpdateRequest;
import com.papeleriafundadores.papeleria.dto.response.OrderResponse;
import com.papeleriafundadores.papeleria.entity.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse createFromCart(OrderRequest request); // Toma el carrito activo del cliente y lo vuelve pedido

    OrderResponse findById(Long id);

    OrderResponse findByNumber(String orderNumber);

    List<OrderResponse> findByCustomer(Long customerId);

    List<OrderResponse> findByStatus(OrderStatus status);

    List<OrderResponse> findAll();

    OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request);

    void cancelOrder(Long id, Long customerId); // Solo si no ha sido preparado (Regla de negocio)
}
