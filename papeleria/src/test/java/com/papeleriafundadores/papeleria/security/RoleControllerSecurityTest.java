package com.papeleriafundadores.papeleria.security;

import com.papeleriafundadores.papeleria.config.SecurityConfig;
import com.papeleriafundadores.papeleria.controller.RoleController;
import com.papeleriafundadores.papeleria.dto.response.RoleResponse;
import com.papeleriafundadores.papeleria.service.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de seguridad para RoleController.
 * Solo ADMIN puede gestionar roles (RF006 / RN013).
 * Roles reales en BD: ADMIN, EMPLEADO, CLIENTE (todo mayúsculas).
 */
@WebMvcTest(RoleController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, CustomUserDetailsService.class,
        SecurityEntryPoints.class, SecurityUtils.class})
class RoleControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private com.papeleriafundadores.papeleria.repository.UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("SEGURIDAD: un CLIENTE NO puede listar los roles del sistema")
    @WithMockUser(username = "cliente@test.com", roles = {"CLIENTE"})
    void clienteNoPuedeListarRoles() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEGURIDAD: un EMPLEADO NO puede listar los roles del sistema")
    @WithMockUser(username = "empleado@test.com", roles = {"EMPLEADO"})
    void empleadoNoPuedeListarRoles() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEGURIDAD: un ADMIN SÍ puede listar los roles del sistema")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void adminPuedeListarRoles() throws Exception {
        when(roleService.findAll()).thenReturn(List.of(
                new RoleResponse(1L, "ADMIN", "Acceso total"),
                new RoleResponse(2L, "EMPLEADO", "Acceso operativo"),
                new RoleResponse(3L, "CLIENTE", "Acceso e-commerce")
        ));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    @DisplayName("SEGURIDAD: un CLIENTE NO puede crear un nuevo rol")
    @WithMockUser(username = "cliente@test.com", roles = {"CLIENTE"})
    void clienteNoPuedeCrearRol() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .param("name", "SUPER_ADMIN")
                        .param("description", "Intento malicioso"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEGURIDAD: un ADMIN SÍ puede crear un nuevo rol")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void adminPuedeCrearRol() throws Exception {
        when(roleService.create("NUEVO_ROL", "Descripción"))
                .thenReturn(new RoleResponse(4L, "NUEVO_ROL", "Descripción"));

        mockMvc.perform(post("/api/v1/roles")
                        .param("name", "NUEVO_ROL")
                        .param("description", "Descripción"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("NUEVO_ROL"));
    }
}
