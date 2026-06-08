package com.loanmanager.backend.dto.request;
 
import com.loanmanager.backend.entity.Prestamo;
import jakarta.validation.constraints.*;
import lombok.Data;
 
import java.math.BigDecimal;
 
@Data
public class PrestamoRequestDTO {
 
    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;
 
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "100000.00", message = "El monto mínimo es $100,000")
    @DecimalMax(value = "500000000.00", message = "El monto máximo es $500,000,000")
    private BigDecimal monto;
 
    @NotNull(message = "La tasa de interés es obligatoria")
    @DecimalMin(value = "0.001", message = "La tasa mínima es 0.1%")
    @DecimalMax(value = "0.05",  message = "La tasa máxima es 5% mensual")
    private BigDecimal tasaInteres;
 
    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1,   message = "El plazo mínimo es 1 mes")
    @Max(value = 120, message = "El plazo máximo es 120 meses")
    private Integer plazoMeses;
 
    private Prestamo.SistemaAmortizacion sistemaAmortizacion
        = Prestamo.SistemaAmortizacion.FRANCES;
 
    private String observaciones;
}