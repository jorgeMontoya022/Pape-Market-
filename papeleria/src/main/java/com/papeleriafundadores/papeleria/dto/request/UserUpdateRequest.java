package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;

public record UserUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String names,
        @NotBlank(message = "El apellido es obligatorio")
        String surnames,
        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone,
        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String address,
        String profilePhoto
) {}
