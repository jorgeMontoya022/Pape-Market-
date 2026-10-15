package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.request.CartItemRequest;
import com.papeleriafundadores.papeleria.dto.response.CartResponse;
import com.papeleriafundadores.papeleria.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Gestión del carrito de compras (RF019-RF022).
 * Accesible a todos los roles autenticados.
 */
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO', 'CLIENTE')")
public class CartController {

    private final CartService cartService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CartResponse> findByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.findByCustomerId(customerId));
    }

    @PostMapping("/customer/{customerId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long customerId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(customerId, request));
    }

    @PutMapping("/customer/{customerId}/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long customerId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(customerId, productId, quantity));
    }

    @DeleteMapping("/customer/{customerId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long customerId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(customerId, productId));
    }

    @DeleteMapping("/customer/{customerId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
