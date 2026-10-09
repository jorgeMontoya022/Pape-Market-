package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record SaleRequest(
        Long customerId, // Opcional, puede ser nulo para clientes mostrador anónimos

        @NotNull(message = "El ID del empleado que atiende es obligatorio")
        Long userId,

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        @Min(0) @Max(100)
        BigDecimal discountPercent,

        @NotEmpty(message = "La venta debe contener al menos un producto")
        List<SaleDetailRequest> details
) {}
