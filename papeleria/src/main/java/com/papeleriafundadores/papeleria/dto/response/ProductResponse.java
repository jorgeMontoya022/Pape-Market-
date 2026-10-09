package com.papeleriafundadores.papeleria.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String barcode,
        String brand,
        BigDecimal purchasePrice,
        BigDecimal salePrice,
        Integer stock,
        Integer minimumStock,
        String image,
        Boolean status
) {}
