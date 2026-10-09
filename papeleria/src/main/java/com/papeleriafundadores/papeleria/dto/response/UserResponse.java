package com.papeleriafundadores.papeleria.dto.response;

import com.papeleriafundadores.papeleria.entity.enums.UserStatus;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String names,
        String surnames,
        String document,
        String email,
        String phone,
        String address,
        String profilePhoto,
        UserStatus state,
        LocalDateTime registrationDate,
        LocalDateTime lastAccess,
        RoleResponse role
) {}
