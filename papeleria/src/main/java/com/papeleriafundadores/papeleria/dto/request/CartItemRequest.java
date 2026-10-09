package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;

public record CartItemRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima a agregar es 1")
        Integer quantity
) {}
