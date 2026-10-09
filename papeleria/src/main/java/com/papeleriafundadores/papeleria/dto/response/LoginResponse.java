package com.papeleriafundadores.papeleria.dto.response;

public record LoginResponse(
        String token,
        String tokenType, // Usualmente "Bearer"
        UserResponse user
) {}
