package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank(message = "El nombre de la empresa es obligatorio")
        String companyName,

        @NotBlank(message = "El NIT es obligatorio")
        String nit,

        @Size(max = 150, message = "El representante legal no puede superar los 150 caracteres")
        String legalRepresentative,

        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        String email,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone,

        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String address,

        @Size(max = 100, message = "La ciudad no puede superar los 100 caracteres")
        String city
) {

}
