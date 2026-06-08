package com.loanmanager.backend.dto.request;
 
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
 
// @Data de Lombok genera: getters, setters, equals, hashCode y toString.
// Es el atajo perfecto para DTOs simples.
@Data
public class LoginRequestDTO {
 
    // @NotBlank: no puede ser null, vacío, ni solo espacios.
    // message: mensaje personalizado de error de validación.
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
 
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}