package com.loanmanager.backend.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
 
    private Long id;
    private String cedula;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudad;
    private LocalDate fechaNacimiento;
    private BigDecimal ingresoMensual;
    private Boolean activo;
    private Integer totalPrestamos;       // cuántos préstamos tiene
    private LocalDateTime createdAt;
}