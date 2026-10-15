package com.papeleriafundadores.papeleria.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.papeleriafundadores.papeleria.config.SecurityConfig;
import com.papeleriafundadores.papeleria.controller.ProductController;
import com.papeleriafundadores.papeleria.dto.request.ProductRequest;
import com.papeleriafundadores.papeleria.dto.response.ProductResponse;
import com.papeleriafundadores.papeleria.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de seguridad para ProductController.
 * Lectura: todos los roles autenticados.
 * Escritura: solo ADMIN (RF007-RF009 / RN012).
 * Roles reales en BD: ADMIN, EMPLEADO, CLIENTE (todo mayúsculas).
 */
@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, CustomUserDetailsService.class,
        SecurityEntryPoints.class, SecurityUtils.class})
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private com.papeleriafundadores.papeleria.repository.UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    private ProductResponse mockProduct() {
        return new ProductResponse(
                1L, "Cuaderno", "Cuaderno 100 hojas", "123456",
                "Norma", new BigDecimal("3.50"), new BigDecimal("5.00"),
                100, 10, null, true
        );
    }

    private ProductRequest mockProductRequest() {
        return new ProductRequest(
                "Cuaderno", "Cuaderno 100 hojas", "123456",
                "Norma", new BigDecimal("3.50"), new BigDecimal("5.00"),
                100, 10, null, 1L, 1L
        );
    }

    @Test
    @DisplayName("RBAC: un CLIENTE SÍ puede consultar productos del catálogo")
    @WithMockUser(username = "cliente@test.com", roles = {"CLIENTE"})
    void clientePuedeConsultarProductos() throws Exception {
        when(productService.findAll()).thenReturn(List.of(mockProduct()));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("RBAC: un CLIENTE NO puede crear productos (solo ADMIN)")
    @WithMockUser(username = "cliente@test.com", roles = {"CLIENTE"})
    void clienteNoPuedeCrearProducto() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockProductRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: un EMPLEADO SÍ puede consultar productos (necesario para ventas)")
    @WithMockUser(username = "empleado@test.com", roles = {"EMPLEADO"})
    void empleadoPuedeConsultarProductos() throws Exception {
        when(productService.findById(1L)).thenReturn(mockProduct());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cuaderno"));
    }

    @Test
    @DisplayName("RBAC: un EMPLEADO NO puede crear productos (solo ADMIN)")
    @WithMockUser(username = "empleado@test.com", roles = {"EMPLEADO"})
    void empleadoNoPuedeCrearProducto() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockProductRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: un ADMIN SÍ puede crear productos")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void adminPuedeCrearProducto() throws Exception {
        when(productService.create(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockProduct());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockProductRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cuaderno"));
    }

    @Test
    @DisplayName("RBAC: un ADMIN SÍ puede desactivar un producto (eliminación lógica)")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void adminPuedeToggleProducto() throws Exception {
        mockMvc.perform(patch("/api/v1/products/1/toggle"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("RBAC: un EMPLEADO NO puede desactivar productos (solo ADMIN / RN012)")
    @WithMockUser(username = "empleado@test.com", roles = {"EMPLEADO"})
    void empleadoNoPuedeToggleProducto() throws Exception {
        mockMvc.perform(patch("/api/v1/products/1/toggle"))
                .andExpect(status().isForbidden());
    }
}
