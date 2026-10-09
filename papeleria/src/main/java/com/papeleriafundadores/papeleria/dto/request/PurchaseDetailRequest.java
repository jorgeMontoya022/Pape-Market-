package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PurchaseDetailRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad comprada es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mínimo 1")
        Integer quantity,

        @NotNull(message = "El costo unitario pactado es obligatorio")
        @Positive(message = "El costo unitario debe ser mayor a 0")
        BigDecimal unitCost
) {}
