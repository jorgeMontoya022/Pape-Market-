package com.papeleriafundadores.papeleria.security;

import com.papeleriafundadores.papeleria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Utilidades de seguridad accesibles desde expresiones @PreAuthorize.
 *
 * Spring Security permite inyectar beans en expresiones SpEL usando la sintaxis
 * @beanName.metodo(...), por ejemplo:
 *
 *   @PreAuthorize("@securityUtils.isOwnerOrAdmin(#id, authentication)")
 *
 * Esto evita lógica de autorización dispersa en los controladores y permite
 * reutilizar la misma regla en múltiples endpoints sin duplicar código.
 *
 * El nombre del bean en el contexto de Spring es "securityUtils" (primera letra
 * en minúscula por convención), que es exactamente lo que se usa en @PreAuthorize.
 */
@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Verifica si el usuario autenticado es dueño del recurso identificado por
     * el userId dado, O si tiene rol de Administrador.
     *
     * Uso en controlador:
     *   @PreAuthorize("@securityUtils.isOwnerOrAdmin(#id, authentication)")
     *
     * @param userId        ID del usuario dueño del recurso (viene del path variable)
     * @param authentication objeto de autenticación inyectado automáticamente por Spring
     * @return true si el usuario autenticado tiene rol Administrador
     *         o si su email coincide con el del usuario del recurso solicitado
     */
    public boolean isOwnerOrAdmin(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Si es ADMIN, acceso total
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return true;
        }

        // Si no es admin, verificamos que el email del token corresponda al userId
        // El "username" en el UserDetails es el email del usuario
        String authenticatedEmail = authentication.getName();
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(authenticatedEmail))
                .orElse(false);
    }

    /**
     * Verifica si el usuario autenticado tiene al menos uno de los roles indicados.
     * Útil para expresiones con múltiples roles permitidos.
     *
     * Uso:
     *   @PreAuthorize("@securityUtils.hasAnyRole(authentication, 'Administrador', 'Empleado')")
     *
     * @param authentication objeto de autenticación
     * @param roles          roles a verificar (sin prefijo ROLE_)
     * @return true si el usuario tiene alguno de los roles
     */
    public boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (String role : roles) {
            boolean match = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
            if (match) return true;
        }
        return false;
    }
}
