package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.response.RoleResponse;
import com.papeleriafundadores.papeleria.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de roles del sistema.
 * Solo ADMIN puede ver y crear roles (RF006 / RN013).
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleResponse> create(
            @RequestParam String name,
            @RequestParam String description) {
        return new ResponseEntity<>(roleService.create(name, description), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<RoleResponse> findByName(@PathVariable String name) {
        return ResponseEntity.ok(roleService.findByName(name));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> findAll() {
        return ResponseEntity.ok(roleService.findAll());
    }
}
