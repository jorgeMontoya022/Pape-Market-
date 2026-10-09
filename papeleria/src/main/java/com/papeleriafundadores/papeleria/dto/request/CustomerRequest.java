package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;

public record CustomerRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String names,

        @NotBlank(message = "El apellido es obligatorio")
        String surnames,

        @NotBlank(message = "El documento es obligatorio")
        @Size(max = 15)
        String document,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo no es válido")
        String email,

        String phone,
        String address
) {}
