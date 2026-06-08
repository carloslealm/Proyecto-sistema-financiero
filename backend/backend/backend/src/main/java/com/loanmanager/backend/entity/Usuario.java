// Archivo: src/main/java/com/loanmanager/backend/entity/Usuario.java
package com.loanmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity implements UserDetails {
    // ↑ Implementamos UserDetails de Spring Security.
    // Esto le dice a Spring: "Esta entidad ES un usuario
    // del sistema de autenticación." Spring Security usará
    // getUsername(), getPassword() y getAuthorities()
    // automáticamente al validar cada petición HTTP.

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    // unique = true: no pueden existir dos usuarios con el mismo email.
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Nunca llamamos este campo "password" directamente en respuestas.
    // Los DTOs de respuesta NUNCA incluirán este campo.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 20)
    private String telefono;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    // ── Relación N:M con Rol ──────────────────────────────────
    // @JoinTable define la tabla intermedia "usuario_rol".
    //   joinColumns: FK del lado de Usuario (usuario_id)
    //   inverseJoinColumns: FK del lado de Rol (rol_id)
    // CascadeType.MERGE: si actualizas un usuario con roles nuevos,
    // se actualizan también en usuario_rol.
    // PERSIST NO está aquí intencionalmente: no queremos que al
    // crear un usuario se creen roles nuevos accidentalmente.
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "usuario_rol",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    // ── Métodos de UserDetails ────────────────────────────────
    // Spring Security llama a estos métodos internamente.
    // Los implementamos para conectar nuestra entidad con
    // el sistema de autenticación.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertimos cada Rol en un GrantedAuthority.
        // Spring Security entiende "ROLE_ADMIN", "ROLE_ASESOR", etc.
        // El prefijo "ROLE_" es una convención de Spring Security.
        return roles.stream()
            .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
            .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        // Spring Security busca getPassword(), pero nuestro campo
        // se llama passwordHash. Este método hace el puente.
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        // Usamos el email como identificador único de login.
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Implementación futura: cuentas con expiración
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Implementación futura: bloqueo por intentos fallidos
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Implementación futura: contraseñas con expiración
    }

    @Override
    public boolean isEnabled() {
        // Usamos nuestro campo "activo" para habilitar/deshabilitar.
        // Si activo = false, Spring Security rechaza el login.
        return this.activo;
    }

    // ── Método de utilidad ───────────────────────────────────
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    public boolean tieneRol(String nombreRol) {
        return this.roles.stream()
            .anyMatch(r -> r.getNombre().equalsIgnoreCase(nombreRol));
    }
}