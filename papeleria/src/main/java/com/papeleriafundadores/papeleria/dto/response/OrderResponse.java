package com.papeleriafundadores.papeleria.dto.response;

import com.papeleriafundadores.papeleria.entity.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        LocalDateTime orderDate,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal total,
        String deliveryAddress,
        String notes,
        CustomerResponse customer,
        List<OrderDetailResponse> details
) {}
