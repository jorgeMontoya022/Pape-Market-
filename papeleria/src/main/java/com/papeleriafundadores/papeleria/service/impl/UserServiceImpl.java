package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.dto.request.*;
import com.papeleriafundadores.papeleria.dto.response.LoginResponse;
import com.papeleriafundadores.papeleria.dto.response.UserResponse;
import com.papeleriafundadores.papeleria.entity.Customer;
import com.papeleriafundadores.papeleria.entity.Role;
import com.papeleriafundadores.papeleria.entity.User;
import com.papeleriafundadores.papeleria.entity.enums.CustomerStatus;
import com.papeleriafundadores.papeleria.entity.enums.UserStatus;
import com.papeleriafundadores.papeleria.exception.BadRequestException;
import com.papeleriafundadores.papeleria.exception.DuplicateResourceException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.exception.UserDisabledException;
import com.papeleriafundadores.papeleria.mapper.UserMapper;
import com.papeleriafundadores.papeleria.repository.CustomerRepository;
import com.papeleriafundadores.papeleria.repository.RoleRepository;
import com.papeleriafundadores.papeleria.repository.UserRepository;
import com.papeleriafundadores.papeleria.security.JwtService;
import com.papeleriafundadores.papeleria.service.EmailService;
import com.papeleriafundadores.papeleria.service.UserService;
import com.papeleriafundadores.papeleria.util.EmailTemplateEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository     userRepository;
    private final RoleRepository     roleRepository;
    private final CustomerRepository customerRepository;
    private final UserMapper         userMapper;
    private final PasswordEncoder    passwordEncoder;
    private final EmailService       emailService;
    private final JwtService         jwtService;

    @Value("${app.reset-password-url:http://localhost:8080/api/v1/auth/reset-password}")
    private String resetPasswordUrl;

    // ---------------------------------------------------------------
    // Autenticación
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Credenciales incorrectas");
        }

        if (user.getState() == UserStatus.INACTIVE) {
            throw new UserDisabledException("Tu cuenta está inactiva. Contacta al administrador.");
        }
        if (user.getState() == UserStatus.BLOCKED) {
            throw new UserDisabledException("Tu cuenta ha sido bloqueada por seguridad.");
        }
        if (user.getState() == UserStatus.SUSPENDED) {
            throw new UserDisabledException("Tu cuenta se encuentra suspendida temporalmente.");
        }

        user.setLastAccess(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().getName()
        );

        UserResponse userResponse = userMapper.toResponse(user);
        return new LoginResponse(token, "Bearer", userResponse);
    }

    // ---------------------------------------------------------------
    // Registro
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByDocument(request.document())) {
            throw new DuplicateResourceException("Usuario", "documento", request.document());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Usuario", "Correo", request.email());
        }

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.roleId()));

        User user = userMapper.toEntity(request);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);

        // Si el rol es CLIENTE, crear automáticamente el Customer vinculado.
        // Esto resuelve la desconexión entre User (login) y Customer (pedidos/carrito).
        // Ambos quedan ligados por el campo userId en Customer.
        if ("CLIENTE".equals(role.getName())) {
            createCustomerForUser(savedUser, request);
        }

        return userMapper.toResponse(savedUser);
    }

    /**
     * Crea un Customer a partir de los datos del User recién registrado.
     * Solo se llama cuando el rol del usuario es CLIENTE.
     * Si el email o documento ya existen como Customer, no falla —
     * simplemente no crea el duplicado (el usuario puede seguir usando su cuenta).
     */
    private void createCustomerForUser(User user, UserRegisterRequest request) {
        // Evitar duplicados si por alguna razón ya existe un Customer con ese email o documento
        if (customerRepository.existsByEmail(user.getEmail())) return;
        if (customerRepository.existsByDocument(user.getDocument())) return;

        Customer customer = Customer.builder()
                .names(user.getNames())
                .surnames(user.getSurnames())
                .document(user.getDocument())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .status(CustomerStatus.ACTIVE)
                .userId(user.getId())   // vínculo con el User
                .build();

        customerRepository.save(customer);
    }

    // ---------------------------------------------------------------
    // CRUD de usuario
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        user.setNames(request.names());
        user.setSurnames(request.surnames());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setProfilePhoto(request.profilePhoto());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo", email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByState(UserStatus state) {
        return userRepository.findByState(state).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void changeStatus(Long id, UserStatus newStatus) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        user.setState(newStatus);
        userRepository.save(user);
    }

    // ---------------------------------------------------------------
    // Endpoints /me — perfil propio sin IDOR
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UserResponse findMe(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo", email));
    }

    @Override
    @Transactional
    public UserResponse updateMe(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo", email));
        user.setNames(request.names());
        user.setSurnames(request.surnames());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setProfilePhoto(request.profilePhoto());
        return userMapper.toResponse(userRepository.save(user));
    }

    // ---------------------------------------------------------------
    // Recuperación de contraseña
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo", request.email()));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiration(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String subject = "Recuperación de Contraseña - Papelería Fundadores";
        String resetUrl = resetPasswordUrl + "?token=" + token;
        String htmlBody = EmailTemplateEngine.getResetPasswordTemplate(user.getNames(), resetUrl);
        emailService.sendHtmlEmail(user.getEmail(), subject, htmlBody);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.token())
                .orElseThrow(() -> new BadRequestException("El token de seguridad no es válido."));

        // Verificar que el token no haya expirado
        if (user.getTokenExpiration() != null &&
            user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token de seguridad ha expirado. Solicita uno nuevo.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setTokenExpiration(null);
        userRepository.save(user);
    }
}
