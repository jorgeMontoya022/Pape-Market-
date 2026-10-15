package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.CartItemRequest;
import com.papeleriafundadores.papeleria.dto.response.CartResponse;

public interface CartService {
    CartResponse findByCustomerId(Long customerId);

    CartResponse addItem(Long customerId, CartItemRequest request);

    CartResponse updateItemQuantity(Long customerId, Long productId, Integer quantity);

    CartResponse removeItem(Long customerId, Long productId);

    void clearCart(Long customerId);
}
