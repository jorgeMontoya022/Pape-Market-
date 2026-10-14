package com.papeleriafundadores.papeleria.security;

import com.papeleriafundadores.papeleria.entity.User;
import com.papeleriafundadores.papeleria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link UserDetailsService} de Spring Security.
 *
 * Spring Security llama a este servicio durante la autenticación para cargar
 * los datos del usuario desde la base de datos usando el email como identificador.
 *
 * El rol se mapea a un {@link SimpleGrantedAuthority} con el prefijo "ROLE_"
 * que Spring Security requiere para las comprobaciones con hasRole().
 * Por ejemplo, si el rol en BD es "ADMIN", la authority será "ROLE_ADMIN".
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por su email (usado como "username" en Spring Security).
     *
     * @param email correo electrónico del usuario
     * @return {@link UserDetails} con credenciales y authorities
     * @throws UsernameNotFoundException si no existe un usuario con ese email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Construye la authority a partir del rol del usuario.
        // Spring Security espera el prefijo "ROLE_" para hasRole() / hasAuthority().
        String authority = "ROLE_" + user.getRole().getName();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())           // contraseña ya encriptada con BCrypt
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .accountLocked(isLocked(user))          // bloquea si el estado lo indica
                .disabled(isDisabled(user))             // deshabilita si la cuenta está inactiva
                .build();
    }

    // ---------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------

    private boolean isDisabled(User user) {
        return switch (user.getState()) {
            case INACTIVE, SUSPENDED -> true;
            default                  -> false;
        };
    }

    private boolean isLocked(User user) {
        return switch (user.getState()) {
            case BLOCKED -> true;
            default      -> false;
        };
    }
}
