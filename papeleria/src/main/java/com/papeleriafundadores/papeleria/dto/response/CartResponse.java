package com.papeleriafundadores.papeleria.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        Long id,
        LocalDateTime createdAt,
        BigDecimal total,
        Long customerId,
        List<CartItemResponse> items
) {}
