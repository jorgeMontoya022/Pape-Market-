package com.papeleriafundadores.papeleria.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.papeleriafundadores.papeleria.config.SecurityConfig;
import com.papeleriafundadores.papeleria.controller.UserController;
import com.papeleriafundadores.papeleria.dto.request.UserUpdateRequest;
import com.papeleriafundadores.papeleria.dto.response.RoleResponse;
import com.papeleriafundadores.papeleria.dto.response.UserResponse;
import com.papeleriafundadores.papeleria.entity.enums.UserStatus;
import com.papeleriafundadores.papeleria.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de seguridad para UserController.
 *
 * Los roles en BD son: ADMIN, EMPLEADO, CLIENTE (todo mayúsculas).
 * @WithMockUser(roles={"ADMIN"}) genera la authority ROLE_ADMIN,
 * que coincide exactamente con hasRole('ADMIN') en @PreAuthorize.
 *
 * Escenarios cubiertos:
 *   1. CLIENTE NO puede ver el perfil de otro usuario → 403
 *   2. EMPLEADO NO puede ver el perfil de otro usuario → 403
 *   3. ADMIN SÍ puede ver cualquier perfil → 200
 *   4. Cualquier autenticado SÍ puede ver su propio perfil (/me) → 200
 *   5. Cualquier autenticado SÍ puede editar su propio perfil (/me) → 200
 *   6. Sin autenticación → 401
 *   7. EMPLEADO NO puede cambiar estado de un usuario → 403
 *   8. ADMIN SÍ puede cambiar estado de un usuario → 204
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, CustomUserDetailsService.class,
        SecurityEntryPoints.class, SecurityUtils.class})
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private com.papeleriafundadores.papeleria.repository.UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    private UserResponse mockUserResponse() {
        return new UserResponse(
                1L, "Juan", "Pérez", "1234567890",
                "juan@test.com", "3001234567", "Calle 1",
                null, UserStatus.ACTIVE, LocalDateTime.now(), null,
                new RoleResponse(1L, "ADMIN", "Rol de administrador")
        );
    }

    @Test
    @DisplayName("SEGURIDAD: un CLIENTE NO puede ver el perfil de otro usuario (IDOR bloqueado)")
    @WithMockUser(username = "cliente@test.com", roles = {"CLIENTE"})
    void clienteNoPuedeVerPerfilDeOtroUsuario() throws Exception {
        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEGURIDAD: un EMPLEADO NO puede ver el perfil de otro usuario")
    @WithMockUser(username = "empleado@test.com", roles = {"EMPLEADO"})
    void empleadoNoPuedeVerPerfilDeOtroUsuario() throws Exception {
        mockMvc.perform(get("/api/v1/users/5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEGURIDAD: un ADMIN SÍ puede ver cualquier perfil de usuario")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void adminPuedeVerCualquierPerfil() throws Exception {
        when(userService.findById(1L)).thenReturn(mockUserResponse());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    @DisplayName("SEGURIDAD: un CLIENTE SÍ puede ver su propio perfil via /me")
    @WithMockUser(username = "cliente@test.com", roles = {"CLIENTE"})
    void clientePuedeVerSuPropioPerfilMe() throws Exception {
        UserResponse myProfile = new UserResponse(
                2L, "María", "López", "9876543210",
                "cliente@test.com", null, null, null,
                UserStatus.ACTIVE, LocalDateTime.now(), null,
                new RoleResponse(3L, "CLIENTE", "Rol de cliente")
        );
        when(userService.findMe("cliente@test.com")).thenReturn(myProfile);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cliente@test.com"));
    }

    @Test
    @DisplayName("SEGURIDAD: un EMPLEADO SÍ puede editar su propio perfil via /me")
    @WithMockUser(username = "empleado@test.com", roles = {"EMPLEADO"})
    void empleadoPuedeEditarSuPropioPerfilMe() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest(
                "Carlos", "Gómez", "3009999999", "Carrera 5", null
        );
        UserResponse updated = new UserResponse(
                3L, "Carlos", "Gómez", "1111111111",
                "empleado@test.com", "3009999999", "Carrera 5", null,
                UserStatus.ACTIVE, LocalDateTime.now(), null,
                new RoleResponse(2L, "EMPLEADO", "Rol de empleado")
        );
        when(userService.updateMe(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").value("Carlos"));
    }

    @Test
    @DisplayName("SEGURIDAD: petición sin autenticación a /me recibe 401")
    void peticionSinTokenRecibe401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEGURIDAD: un EMPLEADO NO puede cambiar el estado de un usuario")
    @WithMockUser(username = "empleado@test.com", roles = {"EMPLEADO"})
    void empleadoNoPuedeCambiarEstadoDeUsuario() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1/status")
                        .param("status", "BLOCKED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEGURIDAD: un ADMIN SÍ puede cambiar el estado de un usuario")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void adminPuedeCambiarEstadoDeUsuario() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1/status")
                        .param("status", "BLOCKED"))
                .andExpect(status().isNoContent());
    }
}
