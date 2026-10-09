package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo no es valido")
        String email

) {
}
