package com.loanmanager.backend.service.impl;
 
import com.loanmanager.backend.dto.request.LoginRequestDTO;
import com.loanmanager.backend.dto.request.RegistroRequestDTO;
import com.loanmanager.backend.dto.response.AuthResponseDTO;
import com.loanmanager.backend.entity.Rol;
import com.loanmanager.backend.entity.Usuario;
import com.loanmanager.backend.exception.BadRequestException;
import com.loanmanager.backend.exception.ResourceNotFoundException;
import com.loanmanager.backend.repository.RolRepository;
import com.loanmanager.backend.repository.UsuarioRepository;
import com.loanmanager.backend.service.AuthService;
import com.loanmanager.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
 
// @RequiredArgsConstructor genera un constructor con todos los campos
// marcados como 'final'. Spring detecta ese constructor y hace
// inyección de dependencias automáticamente.
// Es la forma moderna de inyectar — reemplaza @Autowired en campos.
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
 
    // Al ser 'final', Lombok los incluye en el constructor generado.
    // Spring inyecta las implementaciones automáticamente.
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
 
    // ── Login ─────────────────────────────────────────────────
 
    @Override
    @Transactional  // Si algo falla a mitad, hace rollback automático
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.debug("Intento de login para email: {}", request.getEmail());
 
        try {
            // authenticate() verifica email + contraseña contra la BD.
            // Internamente llama a UserDetailsService.loadUserByUsername()
            // y luego a passwordEncoder.matches().
            // Si las credenciales son incorrectas, lanza BadCredentialsException.
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
 
            // Si llegamos aquí, la autenticación fue exitosa.
            Usuario usuario = (Usuario) auth.getPrincipal();
 
            // Generar el JWT para este usuario
            String token = jwtUtil.generarToken(usuario);
 
            // Registrar el último acceso (auditoría)
            usuarioRepository.actualizarUltimoAcceso(
                usuario.getId(), LocalDateTime.now()
            );
 
            log.info("Login exitoso para usuario: {}", usuario.getEmail());
 
            return buildAuthResponse(usuario, token);
 
        } catch (BadCredentialsException e) {
            // Nunca digas "contraseña incorrecta" — da pistas a atacantes.
            // Siempre di "credenciales inválidas".
            log.warn("Credenciales incorrectas para email: {}", request.getEmail());
            throw new BadRequestException("Credenciales inválidas");
        }
    }
 
    // ── Registro ──────────────────────────────────────────────
 
    @Override
    @Transactional
    public AuthResponseDTO registro(RegistroRequestDTO request) {
        log.debug("Intento de registro para email: {}", request.getEmail());
 
        // Validación 1: el email no debe existir ya
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                "Ya existe un usuario con el email: " + request.getEmail()
            );
        }
 
        // Validación 2: las contraseñas deben coincidir
        if (!request.getPassword().equals(request.getConfirmarPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }
 
        // Obtener el rol por defecto (ASESOR para nuevos registros)
        Rol rolAsesor = rolRepository.findByNombre("ASESOR")
            .orElseThrow(() -> new ResourceNotFoundException(
                "Rol ASESOR no encontrado. Ejecute los seeders."
            ));
 
        // Construir la entidad Usuario usando el Builder
        Usuario nuevoUsuario = Usuario.builder()
            .nombre(request.getNombre())
            .apellido(request.getApellido())
            .email(request.getEmail())
            // BCrypt hashea la contraseña con salt aleatorio.
            // Nunca guardes contraseñas en texto plano.
            // BCrypt es lento por diseño — dificulta ataques de fuerza bruta.
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .telefono(request.getTelefono())
            .activo(true)
            .roles(Set.of(rolAsesor))
            .build();
 
        Usuario usuario = usuarioRepository.save(nuevoUsuario);
 
        String token = jwtUtil.generarToken(usuario);
 
        log.info("Nuevo usuario registrado: {}", usuario.getEmail());
 
        return buildAuthResponse(usuario, token);
    }
 
    // ── Método privado de utilidad ────────────────────────────
 
    private AuthResponseDTO buildAuthResponse(Usuario usuario, String token) {
        List<String> roles = usuario.getRoles().stream()
            .map(r -> "ROLE_" + r.getNombre())
            .collect(Collectors.toList());
 
        return AuthResponseDTO.builder()
            .token(token)
            .tipo("Bearer")
            .expiracionMs(86400000L)
            .usuarioId(usuario.getId())
            .nombre(usuario.getNombre())
            .apellido(usuario.getApellido())
            .email(usuario.getEmail())
            .roles(roles)
            .build();
    }
}