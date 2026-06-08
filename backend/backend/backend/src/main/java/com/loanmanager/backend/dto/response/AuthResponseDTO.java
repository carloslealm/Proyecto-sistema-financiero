package com.loanmanager.backend.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.util.List;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
 
    // El token JWT que el frontend guardará en localStorage
    // y enviará en cada petición como header Authorization.
    private String token;
 
    // Tipo de token — siempre "Bearer" por convención HTTP.
    @Builder.Default
    private String tipo = "Bearer";
 
    // Cuántos milisegundos dura el token (24h = 86400000).
    // El frontend puede calcular la fecha de expiración con esto.
    private Long expiracionMs;
 
    // Datos básicos del usuario — el frontend los necesita
    // para mostrar el nombre y personalizar la UI sin hacer
    // otro request a /usuarios/perfil.
    private Long usuarioId;
    private String nombre;
    private String apellido;
    private String email;
    private List<String> roles;  // ["ROLE_ADMIN", "ROLE_ASESOR"]
}