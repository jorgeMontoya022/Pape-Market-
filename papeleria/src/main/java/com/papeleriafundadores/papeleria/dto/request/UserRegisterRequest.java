package com.papeleriafundadores.papeleria.dto.request;

import jakarta.validation.constraints.*;

public record UserRegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String names,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String surnames,

        @NotBlank(message = "El documento es obligatorio")
        @Size(max = 10, message = "El documento no puede superar los 100 caracteres")
        String document,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo no es válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone,

        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String address,

        String profilePhoto,

        @NotNull(message = "El ID del rol es obligatorio")
        Long roleId
) {}
