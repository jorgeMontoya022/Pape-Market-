package com.papeleriafundadores.papeleria.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponse(
        Long id,
        LocalDateTime purchaseDate,
        BigDecimal total,
        SupplierResponse supplier,
        UserResponse user,
        List<PurchaseDetailResponse> details
) {}
