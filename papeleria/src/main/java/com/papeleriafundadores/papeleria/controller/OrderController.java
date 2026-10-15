package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.request.OrderRequest;
import com.papeleriafundadores.papeleria.dto.request.OrderStatusUpdateRequest;
import com.papeleriafundadores.papeleria.dto.response.OrderResponse;
import com.papeleriafundadores.papeleria.entity.enums.OrderStatus;
import com.papeleriafundadores.papeleria.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ---------------------------------------------------------------
    // Crear y cancelar — todos los roles (RF023)
    // ADMIN y EMPLEADO crean pedidos en tienda; CLIENTE los crea online
    // ---------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<OrderResponse> createFromCart(@Valid @RequestBody OrderRequest request) {
        return new ResponseEntity<>(orderService.createFromCart(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @RequestParam Long customerId) {
        orderService.cancelOrder(id, customerId);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------
    // Consulta individual — todos los roles
    // ---------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<OrderResponse> findByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.findByNumber(orderNumber));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
    public ResponseEntity<List<OrderResponse>> findByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.findByCustomer(customerId));
    }

    // ---------------------------------------------------------------
    // Gestión operativa — ADMIN y EMPLEADO (RF025)
    // ---------------------------------------------------------------

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<OrderResponse>> findByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.findByStatus(status));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }
}
