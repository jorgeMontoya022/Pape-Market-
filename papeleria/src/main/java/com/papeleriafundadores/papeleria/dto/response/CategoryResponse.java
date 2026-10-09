package com.papeleriafundadores.papeleria.dto.response;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Boolean status
) {}
