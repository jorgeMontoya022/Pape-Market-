package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.request.SupplierRequest;
import com.papeleriafundadores.papeleria.dto.response.SupplierResponse;
import com.papeleriafundadores.papeleria.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // ---------------------------------------------------------------
    // Escritura — solo ADMIN (RF013)
    // ---------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return new ResponseEntity<>(supplierService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        supplierService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------
    // Lectura — ADMIN y EMPLEADO
    // ---------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<SupplierResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @GetMapping("/nit/{nit}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<SupplierResponse> findByNit(@PathVariable String nit) {
        return ResponseEntity.ok(supplierService.findByNit(nit));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<SupplierResponse>> findAll() {
        return ResponseEntity.ok(supplierService.findAll());
    }
}
