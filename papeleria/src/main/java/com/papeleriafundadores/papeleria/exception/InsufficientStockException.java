package com.papeleriafundadores.papeleria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, Integer availableStock, Integer requestedQuantity) {
        super(String.format("Stock insuficiente para el producto '%s'. Disponible: %d, Solicitado: %d",
                productName, availableStock, requestedQuantity));
    }
}
