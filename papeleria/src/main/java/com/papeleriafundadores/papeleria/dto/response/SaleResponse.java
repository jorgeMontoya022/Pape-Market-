package com.papeleriafundadores.papeleria.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        LocalDateTime saleDate,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal total,
        CustomerResponse customer, // Puede llegar nulo en el JSON si no se registró cliente
        UserResponse user,
        List<SaleDetailResponse> details
) {}
