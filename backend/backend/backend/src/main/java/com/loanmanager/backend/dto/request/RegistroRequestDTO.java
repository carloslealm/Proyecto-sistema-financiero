package com.loanmanager.backend.dto.request;
 
import jakarta.validation.constraints.*;
import lombok.Data;
 
@Data
public class RegistroRequestDTO {
 
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
 
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;
 
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
 
    // Regex de contraseña segura:
    // (?=.*[0-9])      → al menos un dígito
    // (?=.*[a-z])      → al menos una minúscula
    // (?=.*[A-Z])      → al menos una mayúscula
    // (?=.*[@#$!%^&+=]) → al menos un especial
    // .{8,}            → mínimo 8 caracteres
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$!%^&+=]).{8,}$",
        message = "La contraseña debe tener mínimo 8 caracteres, " +
                  "una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String password;
 
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmarPassword;
 
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    private String telefono;
}