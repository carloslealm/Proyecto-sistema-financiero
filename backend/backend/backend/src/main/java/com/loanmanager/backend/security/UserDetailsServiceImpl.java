package com.loanmanager.backend.security;
 
import com.loanmanager.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
 
    private final UsuarioRepository usuarioRepository;
 
    // Spring Security llama a este método durante la autenticación.
    // El parámetro "username" en nuestro caso es el email.
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
 
        return usuarioRepository.findActivoByEmail(email)
            .orElseThrow(() -> {
                log.warn("Usuario no encontrado con email: {}", email);
                // Mensaje genérico — no revelar si el email existe o no
                return new UsernameNotFoundException(
                    "Credenciales inválidas"
                );
            });
        // Nuestra entidad Usuario ya implementa UserDetails,
        // así que la retornamos directamente sin conversión.
    }
}