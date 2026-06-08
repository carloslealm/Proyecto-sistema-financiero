// Archivo: src/main/java/com/loanmanager/backend/config/SecurityConfig.java
package com.loanmanager.backend.config;

import com.loanmanager.backend.security.JwtAuthFilter;
import com.loanmanager.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// @Configuration: esta clase define Beans de Spring (objetos
// gestionados por el contenedor de IoC).
// @EnableWebSecurity: activa la configuración de seguridad web.
// @EnableMethodSecurity: activa @PreAuthorize en los métodos.
//   Permite proteger endpoints con: @PreAuthorize("hasRole('ADMIN')")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // ── Rutas públicas ─────────────────────────────────────────
    // Estas rutas NO requieren token JWT.
    private static final String[] RUTAS_PUBLICAS = {
        "/auth/**",          // login y registro
        "/swagger-ui/**",    // documentación
        "/api-docs/**",      // spec OpenAPI
        "/actuator/health"   // health check para Docker
    };

    // ── SecurityFilterChain ───────────────────────────────────
    // El Bean más importante: define TODAS las reglas de seguridad.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // CSRF: Cross-Site Request Forgery.
            // Lo desactivamos porque usamos JWT (stateless).
            // CSRF protege aplicaciones con sesiones y cookies —
            // nosotros no usamos ninguna de las dos.
            .csrf(AbstractHttpConfigurer::disable)

            // Configuración CORS: permite peticiones desde Angular
            // (que corre en localhost:4200) al backend (localhost:8080).
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Reglas de autorización por ruta
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: sin autenticación
                .requestMatchers(RUTAS_PUBLICAS).permitAll()
                // Solo ADMIN puede gestionar usuarios
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                // ADMIN y ASESOR pueden gestionar préstamos
                .requestMatchers("/prestamos/**")
                    .hasAnyRole("ADMIN", "ASESOR")
                // ADMIN y ASESOR pueden gestionar clientes
                .requestMatchers("/clientes/**")
                    .hasAnyRole("ADMIN", "ASESOR")
                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )

            // STATELESS: el servidor NO guarda sesión entre peticiones.
            // Cada petición debe traer su JWT. Esto hace el backend
            // escalable horizontalmente (múltiples instancias).
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Registrar nuestro AuthenticationProvider
            .authenticationProvider(authenticationProvider())

            // Insertar nuestro filtro JWT ANTES del filtro de
            // autenticación por usuario/contraseña de Spring.
            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // ── CORS Configuration ────────────────────────────────────
    // CORS (Cross-Origin Resource Sharing): política de seguridad
    // del navegador que bloquea peticiones entre orígenes distintos.
    // Angular en :4200 → Backend en :8080 = orígenes distintos.
    // Debemos permitirlo explícitamente.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos — en producción reemplaza con tu dominio
        config.setAllowedOrigins(List.of(
            "http://localhost:4200",   // Angular dev server
            "http://localhost:80",     // Angular en Docker
            "http://localhost"         // Nginx en Docker
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Headers permitidos en las peticiones
        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));

        // Headers expuestos en la respuesta que el JS puede leer
        config.setExposedHeaders(List.of("Authorization"));

        // Permite enviar cookies/credenciales cross-origin
        config.setAllowCredentials(true);

        // Cuánto tiempo el browser cachea la respuesta CORS (1 hora)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        // Aplicar esta configuración a todas las rutas
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ── Beans de autenticación ────────────────────────────────

    // BCrypt: algoritmo de hashing de contraseñas.
    // strength=10: número de rondas de hashing (2^10 = 1024 iteraciones).
    // Más rondas = más seguro pero más lento.
    // 10 es el estándar para producción (tarda ~100ms por hash).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // DaoAuthenticationProvider: el proveedor que Spring Security
    // usa para verificar credenciales.
    // Conecta UserDetailsService (carga el usuario) con
    // PasswordEncoder (verifica la contraseña).
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // AuthenticationManager: el orquestador de la autenticación.
    // Lo inyectamos en AuthServiceImpl para hacer el login.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}