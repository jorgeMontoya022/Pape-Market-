package com.papeleriafundadores.papeleria.dto.response;

import java.math.BigDecimal;

public record OrderDetailResponse(
        Long id,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        ProductResponse product
) {}
