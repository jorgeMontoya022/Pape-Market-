package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.dto.request.SaleDetailRequest;
import com.papeleriafundadores.papeleria.dto.request.SaleRequest;
import com.papeleriafundadores.papeleria.dto.response.SaleResponse;
import com.papeleriafundadores.papeleria.entity.*;
import com.papeleriafundadores.papeleria.exception.InsufficientStockException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.SaleMapper;
import com.papeleriafundadores.papeleria.repository.CustomerRepository;
import com.papeleriafundadores.papeleria.repository.ProductRepository;
import com.papeleriafundadores.papeleria.repository.SaleRepository;
import com.papeleriafundadores.papeleria.repository.UserRepository;
import com.papeleriafundadores.papeleria.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;

    @Override
    @Transactional
    public SaleResponse registerSale(SaleRequest request) {
        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.customerId()));
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.userId()));

        Sale sale = Sale.builder()
                .customer(customer)
                .user(user)
                .subtotal(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .details(new ArrayList<>())
                .build();

        BigDecimal rawSubtotal = BigDecimal.ZERO;

        for (SaleDetailRequest detailRequest : request.details()) {
            Product product = productRepository.findById(detailRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", detailRequest.productId()));

            if (product.getStock() < detailRequest.quantity()) {
                throw new InsufficientStockException(product.getName(), product.getStock(), detailRequest.quantity());
            }

            // Descontar inventario por venta presencial
            product.setStock(product.getStock() - detailRequest.quantity());
            productRepository.save(product);

            BigDecimal lineSubtotal = product.getSalePrice().multiply(BigDecimal.valueOf(detailRequest.quantity()));
            rawSubtotal = rawSubtotal.add(lineSubtotal);

            SaleDetail detail = SaleDetail.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(detailRequest.quantity())
                    .salePrice(product.getSalePrice()) // Se congela el precio de venta del día
                    .subtotal(lineSubtotal)
                    .build();
            sale.getDetails().add(detail);
        }

        // Cálculos matemáticos de la factura de venta
        BigDecimal discountFactor = request.discountPercent().divide(BigDecimal.valueOf(100));
        BigDecimal discountAmount = rawSubtotal.multiply(discountFactor);
        BigDecimal subtotalWithDiscount = rawSubtotal.subtract(discountAmount);

        BigDecimal taxAmount = subtotalWithDiscount.multiply(BigDecimal.valueOf(0.19)); // IVA 19%
        BigDecimal finalTotal = subtotalWithDiscount.add(taxAmount);

        sale.setSubtotal(rawSubtotal);
        sale.setDiscount(discountAmount);
        sale.setTax(taxAmount);
        sale.setTotal(finalTotal);

        return saleMapper.toResponse(saleRepository.save(sale));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse findById(Long id) {
        return saleRepository.findById(id)
                .map(saleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findByUserId(Long userId) {
        return saleRepository.findByUser(userId).stream()
                .map(saleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {
        return saleRepository.findAll().stream()
                .map(saleMapper::toResponse)
                .toList();
    }
}
