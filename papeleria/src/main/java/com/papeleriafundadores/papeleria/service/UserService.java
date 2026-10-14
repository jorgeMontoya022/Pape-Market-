package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.*;
import com.papeleriafundadores.papeleria.dto.response.LoginResponse;
import com.papeleriafundadores.papeleria.dto.response.UserResponse;
import com.papeleriafundadores.papeleria.entity.enums.UserStatus;

import java.util.List;

public interface UserService {

    LoginResponse login(LoginRequest request);

    UserResponse register(UserRegisterRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse findById(Long id);

    UserResponse findByEmail(String email);

    List<UserResponse> findByState(UserStatus state);

    List<UserResponse> findAll();

    void changeStatus(Long id, UserStatus newStatus);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    /**
     * Devuelve el perfil del usuario actualmente autenticado.
     * El email se extrae del SecurityContext, no de un parámetro HTTP.
     */
    UserResponse findMe(String email);

    /**
     * Actualiza el perfil del usuario actualmente autenticado.
     * El ID se resuelve internamente desde el email del JWT.
     */
    UserResponse updateMe(String email, UserUpdateRequest request);
}
