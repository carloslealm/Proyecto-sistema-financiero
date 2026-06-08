// Este filtro se ejecuta UNA VEZ por cada petición HTTP.
// Su trabajo: leer el token JWT, validarlo y cargar el usuario
// en el SecurityContext para que el resto de la app lo conozca.
// ═══════════════════════════════════════════════════════════════
package com.loanmanager.backend.security;
 
import com.loanmanager.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
 
import java.io.IOException;
 
@Slf4j
@Component
@RequiredArgsConstructor
// OncePerRequestFilter garantiza que el filtro se ejecute
// exactamente una vez por petición, sin importar el servidor.
public class JwtAuthFilter extends OncePerRequestFilter {
 
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
 
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
 
        // 1. Leer el header Authorization
        final String authHeader = request.getHeader("Authorization");
 
        // Si no hay header o no empieza con "Bearer ", dejar pasar
        // sin autenticar (las rutas públicas no necesitan token).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
 
        // 2. Extraer el token (quitar el prefijo "Bearer ")
        final String token = authHeader.substring(7);
        final String email;
 
        try {
            email = jwtUtil.extraerEmail(token);
        } catch (Exception e) {
            // Token malformado o con firma inválida
            log.warn("Token JWT inválido: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }
 
        // 3. Si hay email y aún no hay autenticación en el contexto
        // (evitar re-autenticar en la misma petición)
        if (email != null &&
            SecurityContextHolder.getContext().getAuthentication() == null) {
 
            // 4. Cargar el usuario de la BD
            UserDetails userDetails =
                userDetailsService.loadUserByUsername(email);
 
            // 5. Validar que el token corresponde a este usuario
            if (jwtUtil.esTokenValido(token, userDetails)) {
 
                // 6. Crear el objeto de autenticación
                // UsernamePasswordAuthenticationToken con 3 parámetros
                // significa que el usuario YA está autenticado.
                // Con 2 parámetros significaría que falta autenticar.
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,                          // credentials (null post-auth)
                        userDetails.getAuthorities()   // roles/permisos
                    );
 
                // Detalles adicionales de la petición (IP, session, etc.)
                authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                        .buildDetails(request)
                );
 
                // 7. Guardar la autenticación en el contexto de seguridad.
                // A partir de aquí, cualquier parte del código puede hacer:
                // SecurityContextHolder.getContext().getAuthentication()
                // para saber quién está autenticado.
                SecurityContextHolder.getContext()
                    .setAuthentication(authToken);
 
                log.debug("Usuario autenticado via JWT: {}", email);
            }
        }
 
        // 8. Continuar con el siguiente filtro en la cadena
        filterChain.doFilter(request, response);
    }
}