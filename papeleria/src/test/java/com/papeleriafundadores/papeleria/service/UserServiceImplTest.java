package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.LoginRequest;
import com.papeleriafundadores.papeleria.dto.request.ResetPasswordRequest;
import com.papeleriafundadores.papeleria.dto.request.UserRegisterRequest;
import com.papeleriafundadores.papeleria.dto.response.LoginResponse;
import com.papeleriafundadores.papeleria.dto.response.UserResponse;
import com.papeleriafundadores.papeleria.entity.Role;
import com.papeleriafundadores.papeleria.entity.User;
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
import com.papeleriafundadores.papeleria.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UserServiceImpl.
 *
 * Cubre las reglas de negocio críticas:
 *   - Login con credenciales incorrectas lanza BadRequestException
 *   - Login con cuenta inactiva/bloqueada/suspendida lanza UserDisabledException
 *   - Login exitoso devuelve token JWT
 *   - Registro con email duplicado lanza DuplicateResourceException
 *   - Registro con documento duplicado lanza DuplicateResourceException
 *   - Registro CLIENTE crea Customer automáticamente
 *   - Registro ADMIN/EMPLEADO no crea Customer
 *   - resetPassword con token expirado lanza BadRequestException
 *   - resetPassword con token inválido lanza BadRequestException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl — Pruebas unitarias")
class UserServiceImplTest {

    @Mock UserRepository     userRepository;
    @Mock RoleRepository     roleRepository;
    @Mock CustomerRepository customerRepository;
    @Mock UserMapper         userMapper;
    @Mock PasswordEncoder    passwordEncoder;
    @Mock EmailService       emailService;
    @Mock JwtService         jwtService;

    @InjectMocks UserServiceImpl userService;

    private User    activeUser;
    private Role    roleCliente;
    private Role    roleAdmin;

    @BeforeEach
    void setUp() {
        roleCliente = new Role();
        roleCliente.setId(3L);
        roleCliente.setName("CLIENTE");

        roleAdmin = new Role();
        roleAdmin.setId(1L);
        roleAdmin.setName("ADMIN");

        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("jorge@test.com");
        activeUser.setPassword("$2a$10$hashedPassword");
        activeUser.setState(UserStatus.ACTIVE);
        activeUser.setRole(roleCliente);
        activeUser.setNames("Jorge");
        activeUser.setDocument("12345678");
    }

    // ---------------------------------------------------------------
    // login
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Lanza BadRequestException si el email no existe")
        void lanzaExcepcionEmailNoExiste() {
            when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(new LoginRequest("noexiste@test.com", "pass")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Credenciales incorrectas");
        }

        @Test
        @DisplayName("Lanza BadRequestException si la contraseña es incorrecta")
        void lanzaExcepcionContrasenaIncorrecta() {
            when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("wrongpass", activeUser.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> userService.login(new LoginRequest("jorge@test.com", "wrongpass")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Credenciales incorrectas");
        }

        @Test
        @DisplayName("Lanza UserDisabledException si la cuenta está INACTIVA")
        void lanzaExcepcionCuentaInactiva() {
            activeUser.setState(UserStatus.INACTIVE);
            when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            assertThatThrownBy(() -> userService.login(new LoginRequest("jorge@test.com", "pass")))
                    .isInstanceOf(UserDisabledException.class)
                    .hasMessageContaining("inactiva");
        }

        @Test
        @DisplayName("Lanza UserDisabledException si la cuenta está BLOQUEADA")
        void lanzaExcepcionCuentaBloqueada() {
            activeUser.setState(UserStatus.BLOCKED);
            when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            assertThatThrownBy(() -> userService.login(new LoginRequest("jorge@test.com", "pass")))
                    .isInstanceOf(UserDisabledException.class)
                    .hasMessageContaining("bloqueada");
        }

        @Test
        @DisplayName("Lanza UserDisabledException si la cuenta está SUSPENDIDA")
        void lanzaExcepcionCuentaSuspendida() {
            activeUser.setState(UserStatus.SUSPENDED);
            when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            assertThatThrownBy(() -> userService.login(new LoginRequest("jorge@test.com", "pass")))
                    .isInstanceOf(UserDisabledException.class)
                    .hasMessageContaining("suspendida");
        }

        @Test
        @DisplayName("Devuelve LoginResponse con token JWT en login exitoso")
        void devuelveTokenEnLoginExitoso() {
            when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(userRepository.save(activeUser)).thenReturn(activeUser);
            when(jwtService.generateToken("jorge@test.com", "CLIENTE")).thenReturn("jwt.token.fake");
            when(userMapper.toResponse(activeUser)).thenReturn(mock(UserResponse.class));

            LoginResponse response = userService.login(new LoginRequest("jorge@test.com", "pass"));

            assertThat(response.token()).isEqualTo("jwt.token.fake");
            assertThat(response.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Actualiza el último acceso del usuario en login exitoso")
        void actualizaUltimoAccesoEnLoginExitoso() {
            when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(userRepository.save(activeUser)).thenReturn(activeUser);
            when(jwtService.generateToken(anyString(), anyString())).thenReturn("token");
            when(userMapper.toResponse(any())).thenReturn(mock(UserResponse.class));

            userService.login(new LoginRequest("jorge@test.com", "pass"));

            assertThat(activeUser.getLastAccess()).isNotNull();
            verify(userRepository).save(activeUser);
        }

        @Test
        @DisplayName("Mensaje de error genérico — no revela si el email existe")
        void mensajeGenericoNoRevelaEmail() {
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            Throwable emailNoExiste = catchThrowable(() ->
                    userService.login(new LoginRequest("noexiste@test.com", "pass")));

            when(userRepository.findByEmail("jorge@test.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            Throwable passIncorrecta = catchThrowable(() ->
                    userService.login(new LoginRequest("jorge@test.com", "wrong")));

            // Ambos casos devuelven exactamente el mismo mensaje
            assertThat(emailNoExiste.getMessage()).isEqualTo(passIncorrecta.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // register
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("register()")
    class Register {

        private UserRegisterRequest clienteRequest;

        @BeforeEach
        void setupRequest() {
            clienteRequest = new UserRegisterRequest(
                    "Jorge", "Montoya", "12345678",
                    "jorge@test.com", "claveSegura123",
                    "3001234567", "Calle 1", null, 3L
            );
        }

        @Test
        @DisplayName("Lanza DuplicateResourceException si el documento ya existe")
        void lanzaExcepcionDocumentoDuplicado() {
            when(userRepository.existsByDocument("12345678")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(clienteRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("Lanza DuplicateResourceException si el email ya existe")
        void lanzaExcepcionEmailDuplicado() {
            when(userRepository.existsByDocument(anyString())).thenReturn(false);
            when(userRepository.existsByEmail("jorge@test.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(clienteRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException si el rol no existe")
        void lanzaExcepcionRolNoExiste() {
            when(userRepository.existsByDocument(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById(3L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.register(clienteRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Role");
        }

        @Test
        @DisplayName("Crea Customer automáticamente cuando el rol es CLIENTE")
        void creaCustomerAutomaticamenteParaCliente() {
            when(userRepository.existsByDocument(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById(3L)).thenReturn(Optional.of(roleCliente));
            when(userMapper.toEntity(clienteRequest)).thenReturn(activeUser);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(activeUser)).thenReturn(activeUser);
            when(customerRepository.existsByEmail(anyString())).thenReturn(false);
            when(customerRepository.existsByDocument(anyString())).thenReturn(false);
            when(userMapper.toResponse(activeUser)).thenReturn(mock(UserResponse.class));

            userService.register(clienteRequest);

            verify(customerRepository).save(any());
        }

        @Test
        @DisplayName("NO crea Customer cuando el rol es ADMIN")
        void noCreaCustomerParaAdmin() {
            UserRegisterRequest adminRequest = new UserRegisterRequest(
                    "Jorge", "Montoya", "87654321",
                    "admin@test.com", "claveAdmin123",
                    null, null, null, 1L
            );
            User adminUser = new User();
            adminUser.setId(2L);
            adminUser.setRole(roleAdmin);
            adminUser.setEmail("admin@test.com");
            adminUser.setDocument("87654321");

            when(userRepository.existsByDocument(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
            when(userMapper.toEntity(adminRequest)).thenReturn(adminUser);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(adminUser)).thenReturn(adminUser);
            when(userMapper.toResponse(adminUser)).thenReturn(mock(UserResponse.class));

            userService.register(adminRequest);

            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Hashea la contraseña antes de guardar")
        void hashesContrasenaAntesDeGuardar() {
            when(userRepository.existsByDocument(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findById(3L)).thenReturn(Optional.of(roleCliente));
            when(userMapper.toEntity(clienteRequest)).thenReturn(activeUser);
            when(passwordEncoder.encode("claveSegura123")).thenReturn("$2a$hashedValue");
            when(userRepository.save(activeUser)).thenReturn(activeUser);
            when(customerRepository.existsByEmail(anyString())).thenReturn(false);
            when(customerRepository.existsByDocument(anyString())).thenReturn(false);
            when(userMapper.toResponse(activeUser)).thenReturn(mock(UserResponse.class));

            userService.register(clienteRequest);

            assertThat(activeUser.getPassword()).isEqualTo("$2a$hashedValue");
        }
    }

    // ---------------------------------------------------------------
    // resetPassword
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        @Test
        @DisplayName("Lanza BadRequestException si el token no existe")
        void lanzaExcepcionTokenInvalido() {
            when(userRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.resetPassword(new ResetPasswordRequest("invalid-token", "nuevaClave123")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("token");
        }

        @Test
        @DisplayName("Lanza BadRequestException si el token ha expirado")
        void lanzaExcepcionTokenExpirado() {
            activeUser.setResetToken("valid-token");
            activeUser.setTokenExpiration(LocalDateTime.now().minusMinutes(30)); // expirado hace 30 min
            when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(activeUser));

            assertThatThrownBy(() ->
                    userService.resetPassword(new ResetPasswordRequest("valid-token", "nuevaClave123")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("expirado");
        }

        @Test
        @DisplayName("Restablece la contraseña e invalida el token si es válido")
        void restablaceContrasenaConTokenValido() {
            activeUser.setResetToken("valid-token");
            activeUser.setTokenExpiration(LocalDateTime.now().plusMinutes(10)); // vigente
            when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.encode("nuevaClave123")).thenReturn("$2a$nuevaHasheada");
            when(userRepository.save(activeUser)).thenReturn(activeUser);

            userService.resetPassword(new ResetPasswordRequest("valid-token", "nuevaClave123"));

            assertThat(activeUser.getPassword()).isEqualTo("$2a$nuevaHasheada");
            assertThat(activeUser.getResetToken()).isNull();
            assertThat(activeUser.getTokenExpiration()).isNull();
        }
    }
}
