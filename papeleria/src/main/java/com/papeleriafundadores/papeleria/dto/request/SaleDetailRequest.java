package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;

public record SaleDetailRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad vendida es obligatoria")
        @Min(value = 1, message = "La cantidad mínima a vender es 1")
        Integer quantity
) {}
