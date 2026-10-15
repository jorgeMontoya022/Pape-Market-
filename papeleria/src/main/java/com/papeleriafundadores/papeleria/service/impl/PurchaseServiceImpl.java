package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.dto.request.PurchaseDetailRequest;
import com.papeleriafundadores.papeleria.dto.request.PurchaseRequest;
import com.papeleriafundadores.papeleria.dto.response.PurchaseResponse;
import com.papeleriafundadores.papeleria.entity.*;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.PurchaseMapper;
import com.papeleriafundadores.papeleria.repository.ProductRepository;
import com.papeleriafundadores.papeleria.repository.PurchaseRepository;
import com.papeleriafundadores.papeleria.repository.SupplierRepository;
import com.papeleriafundadores.papeleria.repository.UserRepository;
import com.papeleriafundadores.papeleria.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PurchaseMapper purchaseMapper;

    @Override
    @Transactional
    public PurchaseResponse registerPurchase(PurchaseRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", request.supplierId()));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.userId()));

        Purchase purchase = Purchase.builder()
                .supplier(supplier)
                .user(user)
                .total(BigDecimal.ZERO)
                .details(new ArrayList<>())
                .build();

        BigDecimal purchaseTotal = BigDecimal.ZERO;

        for (PurchaseDetailRequest detailRequest : request.details()) {
            Product product = productRepository.findById(detailRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", detailRequest.productId()));

            // Aumentar inventario (Abastecimiento de la papelería)
            product.setStock(product.getStock() + detailRequest.quantity());
            // Actualizar costo de compra histórico en el catálogo si varía
            product.setPurchasePrice(detailRequest.unitCost());
            productRepository.save(product);

            BigDecimal lineSubtotal = detailRequest.unitCost().multiply(BigDecimal.valueOf(detailRequest.quantity()));
            purchaseTotal = purchaseTotal.add(lineSubtotal);

            PurchaseDetails detail = PurchaseDetails.builder()
                    .purchase(purchase)
                    .product(product)
                    .quantity(detailRequest.quantity())
                    .unitCost(detailRequest.unitCost())
                    .subtotal(lineSubtotal)
                    .build();
            purchase.getDetails().add(detail);
        }

        purchase.setTotal(purchaseTotal);
        return purchaseMapper.toResponse(purchaseRepository.save(purchase));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse findById(Long id) {
        return purchaseRepository.findById(id)
                .map(purchaseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Compra", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> findBySupplier(Long supplierId) {
        return purchaseRepository.findBySupplier(supplierId).stream()
                .map(purchaseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> findAll() {
        return purchaseRepository.findAll().stream()
                .map(purchaseMapper::toResponse)
                .toList();
    }
}
