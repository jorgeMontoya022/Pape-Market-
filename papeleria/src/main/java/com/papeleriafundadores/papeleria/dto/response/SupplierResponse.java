package com.papeleriafundadores.papeleria.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupplierResponse(
        Long id,
        @JsonProperty("name")
        String companyName,
        String nit,
        @JsonProperty("contactPerson")
        String legalRepresentative,
        String email,
        String phone,
        String address,
        String city,
        Boolean status
) {}
