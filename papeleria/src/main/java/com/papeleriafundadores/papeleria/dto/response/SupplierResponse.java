package com.papeleriafundadores.papeleria.dto.response;

public record SupplierResponse(
        Long id,
        String companyName,
        String nit,
        String legalRepresentative,
        String email,
        String phone,
        String address,
        String city,
        Boolean status
) {}
