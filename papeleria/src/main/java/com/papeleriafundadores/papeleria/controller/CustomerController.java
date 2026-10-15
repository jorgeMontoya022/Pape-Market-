package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.request.CustomerRequest;
import com.papeleriafundadores.papeleria.dto.response.CustomerResponse;
import com.papeleriafundadores.papeleria.entity.enums.CustomerStatus;
import com.papeleriafundadores.papeleria.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // ---------------------------------------------------------------
    // Registro y consulta — ADMIN y EMPLEADO (RF014)
    // ---------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return new ResponseEntity<>(customerService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @GetMapping("/document/{document}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<CustomerResponse> findByDocument(@PathVariable String document) {
        return ResponseEntity.ok(customerService.findByDocument(document));
    }

    /**
     * Busca el Customer vinculado a un User por su userId.
     * El frontend lo usa justo después del login de un CLIENTE para
     * obtener el customerId necesario para carrito y pedidos.
     * Accesible a todos los roles autenticados porque el propio
     * usuario necesita consultar su propio customer.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomerResponse> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(customerService.findByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<CustomerResponse>> findAll() {
        return ResponseEntity.ok(customerService.findAll());
    }

    // ---------------------------------------------------------------
    // Cambio de estado — solo ADMIN
    // ---------------------------------------------------------------

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam CustomerStatus status) {
        customerService.changeStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
