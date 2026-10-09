package com.papeleriafundadores.papeleria.dto.response;

import java.math.BigDecimal;

public record PurchaseDetailResponse(
        Long id,
        Integer quantity,
        BigDecimal unitCost,
        BigDecimal subtotal,
        ProductResponse product
) {}
