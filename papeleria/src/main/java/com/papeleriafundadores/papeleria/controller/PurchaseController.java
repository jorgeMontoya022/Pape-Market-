package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.request.PurchaseRequest;
import com.papeleriafundadores.papeleria.dto.response.PurchaseResponse;
import com.papeleriafundadores.papeleria.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    // ---------------------------------------------------------------
    // Registro de compra — solo ADMIN (RF026)
    // ---------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PurchaseResponse> registerPurchase(
            @Valid @RequestBody PurchaseRequest request) {
        return new ResponseEntity<>(purchaseService.registerPurchase(request), HttpStatus.CREATED);
    }

    // ---------------------------------------------------------------
    // Consultas — ADMIN y EMPLEADO
    // ---------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<PurchaseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.findById(id));
    }

    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<PurchaseResponse>> findBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(purchaseService.findBySupplier(supplierId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<PurchaseResponse>> findAll() {
        return ResponseEntity.ok(purchaseService.findAll());
    }
}
