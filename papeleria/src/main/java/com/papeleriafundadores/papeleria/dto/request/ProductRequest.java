package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String description,

        @Size(max = 50, message = "El código de barras no puede superar los 50 caracteres")
        String barcode,

        @Size(max = 100, message = "La marca no puede superar los 100 caracteres")
        String brand,

        @NotNull(message = "El precio de compra es obligatorio")
        @PositiveOrZero(message = "El precio de compra no puede ser negativo")
        BigDecimal purchasePrice,

        @NotNull(message = "El precio de venta es obligatorio")
        @PositiveOrZero(message = "El precio de venta no puede ser negativo")
        BigDecimal salePrice,

        @NotNull(message = "El stock inicial es obligatorio")
        @Min(value = 0, message = "El stock no puede ser inferior a 0")
        Integer stock,

        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser inferior a 0")
        Integer minimumStock,

        @Size(max = 255, message = "La ruta de la imagen no puede superar los 255 caracteres")
        @URL(message = "La imagen debe ser una URL con un formato válido (ej: http://... o https://...)")
        String image,

        @NotNull(message = "La categoría es obligatoria")
        Long categoryId,

        @NotNull(message = "El proveedor es obligatorio")
        Long supplierId
) {}
