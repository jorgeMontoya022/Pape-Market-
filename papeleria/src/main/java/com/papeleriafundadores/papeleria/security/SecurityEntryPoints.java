package com.papeleriafundadores.papeleria.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.papeleriafundadores.papeleria.exception.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Manejadores de errores de seguridad a nivel de filtro HTTP.
 *
 * PROBLEMA QUE RESUELVE:
 * Spring Security intercepta AuthenticationException y AccessDeniedException
 * en su propia capa de filtros, ANTES de que la petición llegue al
 * DispatcherServlet donde vive @ControllerAdvice. Por eso, sin estos
 * handlers, los errores 401/403 devuelven HTML en lugar de JSON.
 *
 * SOLUCIÓN:
 * Implementamos AuthenticationEntryPoint (para 401) y AccessDeniedHandler
 * (para 403) que escriben directamente el JSON en la respuesta HTTP,
 * usando el mismo formato ErrorDetails que usa el GlobalExceptionHandler.
 * Ambos se registran en SecurityConfig.
 */
@Component
public class SecurityEntryPoints
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    // ObjectMapper para serializar ErrorDetails a JSON
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Se invoca cuando una petición llega sin token válido a un endpoint protegido.
     * Devuelve 401 Unauthorized con JSON consistente con el resto de la API.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeJsonError(
                response,
                HttpStatus.UNAUTHORIZED,
                "No autenticado. Por favor inicia sesión para acceder a este recurso.",
                "AUTHENTICATION_REQUIRED"
        );
    }

    /**
     * Se invoca cuando un usuario autenticado intenta acceder a un recurso
     * para el que no tiene permiso (falla @PreAuthorize).
     * Devuelve 403 Forbidden con JSON consistente con el resto de la API.
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writeJsonError(
                response,
                HttpStatus.FORBIDDEN,
                "Acceso denegado. No tienes permisos para realizar esta acción.",
                "ACCESS_DENIED"
        );
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private void writeJsonError(HttpServletResponse response,
                                 HttpStatus status,
                                 String message,
                                 String errorCode) throws IOException {
        ErrorDetails body = new ErrorDetails(
                LocalDateTime.now(),
                message,
                "No disponible",
                errorCode
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(response.getWriter(), body);
    }
}
