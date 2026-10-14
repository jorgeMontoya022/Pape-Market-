package com.papeleriafundadores.papeleria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que intercepta cada petición HTTP una sola vez.
 *
 * Flujo:
 *  1. Lee el header "Authorization: Bearer <token>"
 *  2. Si no existe o no empieza con "Bearer ", pasa al siguiente filtro sin autenticar.
 *  3. Si existe, extrae el email del token y carga el usuario desde la base de datos.
 *  4. Valida el token (firma, expiración, subject).
 *  5. Si es válido y el contexto aún no tiene autenticación, la establece en
 *     {@link SecurityContextHolder}.
 *  6. Si el token es inválido o falta, simplemente continúa la cadena sin autenticar.
 *     Spring Security devolverá 401 en los endpoints protegidos por su propia lógica.
 *
 * Al ser stateless, NO se crea sesión HTTP en ningún momento.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER   = "Authorization";

    private final JwtService              jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        // 1. Si no hay header Authorization o no empieza con "Bearer ", no procesamos.
        //    Continuamos la cadena y dejamos que Spring Security rechace si hace falta.
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraemos el token (quitamos el prefijo "Bearer ")
        final String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 3. Extraemos el email del subject del JWT
            final String email = jwtService.extractEmail(token);

            // 4. Solo procesamos si hay email y aún no hay autenticación en el contexto
            //    (evita procesar el mismo token dos veces en la misma petición)
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Cargamos los UserDetails desde la base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 6. Validamos que el token sea auténtico y no esté expirado
                if (jwtService.isTokenValid(token, userDetails.getUsername())) {

                    // 7. Construimos el objeto de autenticación y lo ponemos en el contexto
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                          // credentials: null (ya autenticado)
                                    userDetails.getAuthorities()
                            );

                    // Adjuntamos detalles de la petición (IP, session id, etc.)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token malformado, firma incorrecta, expirado, usuario no encontrado, etc.
            // No lanzamos la excepción — simplemente NO autenticamos.
            // Spring Security se encargará de responder 401 en los endpoints protegidos.
            SecurityContextHolder.clearContext();
        }

        // 8. Continuamos la cadena de filtros en todos los casos
        filterChain.doFilter(request, response);
    }
}
