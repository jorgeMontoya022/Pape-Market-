package com.papeleriafundadores.papeleria.dto.request;

import com.papeleriafundadores.papeleria.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "El nuevo estado del pedido es obligatorio")
        OrderStatus status
) {}
