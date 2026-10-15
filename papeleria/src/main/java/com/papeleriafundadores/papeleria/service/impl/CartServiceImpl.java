package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.dto.request.CartItemRequest;
import com.papeleriafundadores.papeleria.dto.response.CartResponse;
import com.papeleriafundadores.papeleria.entity.Cart;
import com.papeleriafundadores.papeleria.entity.CartItem;
import com.papeleriafundadores.papeleria.entity.Customer;
import com.papeleriafundadores.papeleria.entity.Product;
import com.papeleriafundadores.papeleria.exception.InsufficientStockException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.CartMapper;
import com.papeleriafundadores.papeleria.repository.CartItemRepository;
import com.papeleriafundadores.papeleria.repository.CartRepository;
import com.papeleriafundadores.papeleria.repository.CustomerRepository;
import com.papeleriafundadores.papeleria.repository.ProductRepository;
import com.papeleriafundadores.papeleria.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse findByCustomerId(Long customerId) {
        Cart cart = cartRepository.findByCustomer(customerId)
                .orElseGet(() -> createNewCart(customerId));
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long customerId, CartItemRequest request) {
        Cart cart = cartRepository.findByCustomer(customerId).orElseGet(() -> createNewCart(customerId));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.productId()));

        if (product.getStock() < request.quantity()) {
            throw new InsufficientStockException(product.getName(), product.getStock(), request.quantity());
        }

        CartItem item = cartItemRepository.findItem(cart.getId(), product.getId())
                .orElseGet(() -> CartItem.builder().cart(cart).product(product).quantity(0).build());

        int totalQuantity = item.getQuantity() + request.quantity();
        if (product.getStock() < totalQuantity) {
            throw new InsufficientStockException(product.getName(), product.getStock(), totalQuantity);
        }

        item.setQuantity(totalQuantity);
        item.setUnitPrice(product.getSalePrice());
        item.setSubtotal(product.getSalePrice().multiply(BigDecimal.valueOf(totalQuantity)));
        cartItemRepository.save(item);

        recalculateCartTotal(cart);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long customerId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByCustomer(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito", "clienteId", customerId));
        CartItem item = cartItemRepository.findItem(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de Carrito", "productoId", productId));

        if (item.getProduct().getStock() < quantity) {
            throw new InsufficientStockException(item.getProduct().getName(), item.getProduct().getStock(), quantity);
        }

        item.setQuantity(quantity);
        item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(item);

        recalculateCartTotal(cart);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long customerId, Long productId) {
        Cart cart = cartRepository.findByCustomer(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito", "clienteId", customerId));
        CartItem item = cartItemRepository.findItem(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de Carrito", "productoId", productId));

        cartItemRepository.delete(item);
        cart.getItems().remove(item);

        recalculateCartTotal(cart);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        Cart cart = cartRepository.findByCustomer(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito", "clienteId", customerId));
        cartItemRepository.clearCart(cart.getId());
        cart.setTotal(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    private Cart createNewCart(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", customerId));
        Cart cart = Cart.builder()
                .customer(customer)
                .total(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
        return cartRepository.save(cart);
    }

    private void recalculateCartTotal(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            cart.setTotal(BigDecimal.ZERO);
            return;
        }

        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotal(total);
    }

}
