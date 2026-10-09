package com.papeleriafundadores.papeleria.dto.response;

import java.math.BigDecimal;

public record SaleDetailResponse(
        Long id,
        Integer quantity,
        BigDecimal salePrice,
        BigDecimal subtotal,
        ProductResponse product
) {}
