package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;

public record OrderRequest(
        @NotBlank(message = "La dirección de entrega es obligatoria")
        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String deliveryAddress,

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
        String notes,

        @NotNull(message = "El ID del cliente es obligatorio")
        Long customerId
) {}
