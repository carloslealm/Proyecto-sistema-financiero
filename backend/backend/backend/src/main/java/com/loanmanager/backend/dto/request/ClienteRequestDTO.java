package com.loanmanager.backend.dto.request;
 
import jakarta.validation.constraints.*;
import lombok.Data;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 
@Data
public class ClienteRequestDTO {
 
    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 6, max = 20, message = "La cédula debe tener entre 6 y 20 caracteres")
    private String cedula;
 
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
 
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;
 
    @Email(message = "El formato del email no es válido")
    private String email;
 
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos")
    private String telefono;
 
    private String direccion;
 
    private String ciudad;
 
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;
 
    @DecimalMin(value = "0.0", message = "El ingreso mensual no puede ser negativo")
    private BigDecimal ingresoMensual;
}