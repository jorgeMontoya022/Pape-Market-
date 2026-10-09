package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record PurchaseRequest(
        @NotNull(message = "El ID del proveedor es obligatorio")
        Long supplierId,

        @NotNull(message = "El ID del usuario/empleado que registra es obligatorio")
        Long userId,

        @NotEmpty(message = "La compra debe incluir al menos un producto en el detalle")
        List<PurchaseDetailRequest> details
) {}
