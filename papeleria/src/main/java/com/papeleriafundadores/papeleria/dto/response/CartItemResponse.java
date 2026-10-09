package com.papeleriafundadores.papeleria.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        ProductResponse product
) {}
