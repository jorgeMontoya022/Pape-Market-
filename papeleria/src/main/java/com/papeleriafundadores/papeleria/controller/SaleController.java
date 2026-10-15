package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.request.SaleRequest;
import com.papeleriafundadores.papeleria.dto.response.SaleResponse;
import com.papeleriafundadores.papeleria.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Ventas presenciales y en línea.
 * Solo ADMIN y EMPLEADO pueden registrar y consultar ventas (RF027).
 */
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponse> registerSale(@Valid @RequestBody SaleRequest request) {
        return new ResponseEntity<>(saleService.registerSale(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SaleResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(saleService.findByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>> findAll() {
        return ResponseEntity.ok(saleService.findAll());
    }
}
