package com.loanmanager.backend.controller;
 
import com.loanmanager.backend.dto.request.LoginRequestDTO;
import com.loanmanager.backend.dto.request.RegistroRequestDTO;
import com.loanmanager.backend.dto.response.ApiResponseDTO;
import com.loanmanager.backend.dto.response.AuthResponseDTO;
import com.loanmanager.backend.entity.Usuario;
import com.loanmanager.backend.repository.UsuarioRepository;
import com.loanmanager.backend.service.AuthService;
import com.loanmanager.backend.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.loanmanager.backend.entity.Rol;
import com.loanmanager.backend.exception.ResourceNotFoundException;
import java.util.HashMap;
import java.util.stream.Collectors;

 
// @RestController = @Controller + @ResponseBody
// Significa que todos los métodos retornan datos (JSON),
// no vistas HTML. Spring serializa automáticamente a JSON.
@RestController
// @RequestMapping: prefijo de ruta para todos los endpoints.
// Con el context-path del yml, la ruta completa es:
// /api/v1/auth/login
@RequestMapping("/auth")
@RequiredArgsConstructor
// @Tag: documentación para Swagger
@Tag(name = "Autenticación", description = "Login y registro de usuarios")
public class AuthController {
 
    private final AuthService authService;
 
    // POST /api/v1/auth/login
    @PostMapping("/login")
    // @Operation: documentación del endpoint en Swagger
    @Operation(summary = "Iniciar sesión",
               description = "Autentica un usuario y retorna un token JWT")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(
            // @Valid activa las validaciones del DTO (@NotBlank, @Email, etc.)
            // Si alguna falla, GlobalExceptionHandler la captura.
            @Valid @RequestBody LoginRequestDTO request) {
 
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Login exitoso", response)
        );
    }
 
    // POST /api/v1/auth/registro
    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario",
               description = "Crea un nuevo usuario asesor en el sistema")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> registro(
            @Valid @RequestBody RegistroRequestDTO request) {
 
        AuthResponseDTO response = authService.registro(request);
        // HTTP 201 Created: código semánticamente correcto para creación
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponseDTO.ok("Usuario registrado exitosamente", response));
    }

    @GetMapping("/generate-hash")
public String generateHash(@RequestParam String password) {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10)
        .encode(password);
}

@Autowired
private UsuarioRepository usuarioRepository;

@Autowired
private PasswordEncoder passwordEncoder;

@Autowired
private EmailService emailService;

@PostMapping("/recuperar-password")
public ResponseEntity<ApiResponseDTO<String>> recuperarPassword(
        @RequestBody Map<String, String> request) {

    String email = request.get("email");

    Optional<Usuario> usuarioOpt = usuarioRepository.findActivoByEmail(email);
    if (usuarioOpt.isEmpty()) {
        // Por seguridad no decimos si el email existe o no
        return ResponseEntity.ok(ApiResponseDTO.ok(
            "Si el email existe, recibirás las instrucciones", "OK"));
    }

    // Generar contraseña temporal
    String nuevaPassword = generarPasswordTemporal();
    Usuario usuario = usuarioOpt.get();
    usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
    usuarioRepository.save(usuario);

    // Enviar email
    emailService.enviarRecuperacionPassword(email, nuevaPassword);

    return ResponseEntity.ok(ApiResponseDTO.ok(
        "Si el email existe, recibirás las instrucciones", "OK"));
}

private String generarPasswordTemporal() {
    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#";
    StringBuilder sb = new StringBuilder();
    java.util.Random random = new java.util.Random();
    for (int i = 0; i < 10; i++) {
        sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
}

@GetMapping("/perfil")
public ResponseEntity<ApiResponseDTO<Map<String, Object>>> perfil(
        org.springframework.security.core.Authentication auth) {

    String email = auth.getName();
    Usuario u = usuarioRepository.findActivoByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    Map<String, Object> map = new HashMap<>();
    map.put("id",           u.getId());
    map.put("nombre",       u.getNombre());
    map.put("apellido",     u.getApellido());
    map.put("email",        u.getEmail());
    map.put("telefono",     u.getTelefono() != null ? u.getTelefono() : "");
    map.put("activo",       u.getActivo());
    map.put("roles",        u.getRoles().stream()
                             .map(Rol::getNombre).collect(Collectors.toList()));
    map.put("ultimoAcceso", u.getUltimoAcceso() != null ?
                             u.getUltimoAcceso().toString() : "");
    map.put("createdAt",    u.getCreatedAt().toString());

    return ResponseEntity.ok(ApiResponseDTO.ok("Perfil obtenido", map));
}

@PutMapping("/perfil")
public ResponseEntity<ApiResponseDTO<String>> actualizarPerfil(
        org.springframework.security.core.Authentication auth,
        @RequestBody Map<String, Object> request) {

    String email = auth.getName();
    Usuario u = usuarioRepository.findActivoByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    if (request.containsKey("nombre"))   u.setNombre((String) request.get("nombre"));
    if (request.containsKey("apellido")) u.setApellido((String) request.get("apellido"));
    if (request.containsKey("telefono")) u.setTelefono((String) request.get("telefono"));

    if (request.containsKey("passwordActual") && request.containsKey("passwordNuevo")) {
        String passwordActual = (String) request.get("passwordActual");
        String passwordNuevo  = (String) request.get("passwordNuevo");
        if (!passwordEncoder.matches(passwordActual, u.getPasswordHash())) {
            return ResponseEntity.badRequest()
                .body(ApiResponseDTO.error("La contraseña actual es incorrecta"));
        }
        u.setPasswordHash(passwordEncoder.encode(passwordNuevo));
    }

    usuarioRepository.save(u);
    return ResponseEntity.ok(ApiResponseDTO.ok("Perfil actualizado", "OK"));
}
}