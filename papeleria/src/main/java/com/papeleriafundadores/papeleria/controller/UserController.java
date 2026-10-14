package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.request.UserUpdateRequest;
import com.papeleriafundadores.papeleria.dto.response.UserResponse;
import com.papeleriafundadores.papeleria.entity.enums.UserStatus;
import com.papeleriafundadores.papeleria.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---------------------------------------------------------------
    // Endpoints /me — perfil propio (cualquier usuario autenticado)
    // El email se extrae del JWT, nunca de un parámetro HTTP → sin IDOR
    // ---------------------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.findMe(userDetails.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMe(userDetails.getUsername(), request));
    }

    // ---------------------------------------------------------------
    // Gestión de usuarios — solo ADMIN (RN013)
    // ---------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @GetMapping("/state/{state}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> findByState(@PathVariable UserStatus state) {
        return ResponseEntity.ok(userService.findByState(state));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        userService.changeStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}
