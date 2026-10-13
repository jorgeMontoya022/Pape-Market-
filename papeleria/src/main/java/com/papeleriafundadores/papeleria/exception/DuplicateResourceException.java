package com.papeleriafundadores.papeleria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("El %s con %s '%s' ya se encuentra registrado en el sistema",
                resourceName, fieldName, fieldValue));
    }
}
