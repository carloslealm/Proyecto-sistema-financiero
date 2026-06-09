package com.loanmanager.backend.controller;

import com.loanmanager.backend.dto.response.ApiResponseDTO;
import com.loanmanager.backend.entity.Rol;
import com.loanmanager.backend.entity.Usuario;
import com.loanmanager.backend.exception.ResourceNotFoundException;
import com.loanmanager.backend.repository.RolRepository;
import com.loanmanager.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<Map<String, Object>>>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Usuario> usuarios = usuarioRepository.findAll(PageRequest.of(page, size));
        Page<Map<String, Object>> response = usuarios.map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id",        u.getId());
            map.put("nombre",    u.getNombre());
            map.put("apellido",  u.getApellido());
            map.put("email",     u.getEmail());
            map.put("telefono",  u.getTelefono() != null ? u.getTelefono() : "");
            map.put("activo",    u.getActivo());
            map.put("roles",     u.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList()));
            map.put("createdAt", u.getCreatedAt().toString());
            return map;
        });

        return ResponseEntity.ok(ApiResponseDTO.ok("Usuarios obtenidos", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> obtener(@PathVariable Long id) {
        Usuario u = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Map<String, Object> map = new HashMap<>();
        map.put("id",       u.getId());
        map.put("nombre",   u.getNombre());
        map.put("apellido", u.getApellido());
        map.put("email",    u.getEmail());
        map.put("telefono", u.getTelefono() != null ? u.getTelefono() : "");
        map.put("activo",   u.getActivo());
        map.put("roles",    u.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList()));

        return ResponseEntity.ok(ApiResponseDTO.ok("Usuario obtenido", map));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> crear(
            @RequestBody Map<String, Object> request) {

        String email = (String) request.get("email");
        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                .body(ApiResponseDTO.error("Ya existe un usuario con ese email"));
        }

        Set<Rol> roles = new HashSet<>();
        List<String> roleNames = (List<String>) request.get("roles");
        if (roleNames != null) {
            for (String rolNombre : roleNames) {
                rolRepository.findByNombre(rolNombre).ifPresent(roles::add);
            }
        }
        if (roles.isEmpty()) {
            rolRepository.findByNombre("ASESOR").ifPresent(roles::add);
        }

        Usuario usuario = Usuario.builder()
            .nombre((String) request.get("nombre"))
            .apellido((String) request.get("apellido"))
            .email(email)
            .passwordHash(passwordEncoder.encode((String) request.get("password")))
            .telefono((String) request.get("telefono"))
            .activo(true)
            .roles(roles)
            .build();

        usuarioRepository.save(usuario);

        Map<String, Object> result = new HashMap<>();
        result.put("id",    usuario.getId());
        result.put("email", usuario.getEmail());

        return ResponseEntity.ok(ApiResponseDTO.ok("Usuario creado", result));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponseDTO<String>> cambiarEstado(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setActivo(!usuario.getActivo());
        usuarioRepository.save(usuario);

        String estado = usuario.getActivo() ? "activado" : "desactivado";
        return ResponseEntity.ok(ApiResponseDTO.ok("Usuario " + estado, estado));
    }

    @PatchMapping("/{id}/roles")
    public ResponseEntity<ApiResponseDTO<String>> cambiarRoles(
            @PathVariable Long id,
            @RequestBody Map<String, List<String>> request) {

        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Set<Rol> nuevosRoles = new HashSet<>();
        List<String> roleNames = request.get("roles");
        if (roleNames != null) {
            for (String rolNombre : roleNames) {
                rolRepository.findByNombre(rolNombre).ifPresent(nuevosRoles::add);
            }
        }

        usuario.setRoles(nuevosRoles);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(ApiResponseDTO.ok("Roles actualizados", "OK"));
    }

    @GetMapping("/perfil")
@PreAuthorize("isAuthenticated()")
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
                             .map(Rol::getNombre)
                             .collect(Collectors.toList()));
    map.put("ultimoAcceso", u.getUltimoAcceso() != null ? 
                             u.getUltimoAcceso().toString() : "");
    map.put("createdAt",    u.getCreatedAt().toString());

    return ResponseEntity.ok(ApiResponseDTO.ok("Perfil obtenido", map));
}

@PutMapping("/perfil")
@PreAuthorize("isAuthenticated()")
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