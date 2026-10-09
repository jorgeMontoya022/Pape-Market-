package com.papeleriafundadores.papeleria.dto.response;

import com.papeleriafundadores.papeleria.entity.enums.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String names,
        String surnames,
        String document,
        String email,
        String phone,
        String address,
        CustomerStatus status,
        LocalDateTime registrationDate,
        Long userId         // ID del User vinculado (null si fue creado manualmente)
) {}
