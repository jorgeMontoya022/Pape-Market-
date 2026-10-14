package com.papeleriafundadores.papeleria.controller;

import com.papeleriafundadores.papeleria.dto.request.ForgotPasswordRequest;
import com.papeleriafundadores.papeleria.dto.request.LoginRequest;
import com.papeleriafundadores.papeleria.dto.request.ResetPasswordRequest;
import com.papeleriafundadores.papeleria.dto.request.UserRegisterRequest;
import com.papeleriafundadores.papeleria.dto.response.LoginResponse;
import com.papeleriafundadores.papeleria.dto.response.UserResponse;
import com.papeleriafundadores.papeleria.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
