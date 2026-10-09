package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String description
) {}
