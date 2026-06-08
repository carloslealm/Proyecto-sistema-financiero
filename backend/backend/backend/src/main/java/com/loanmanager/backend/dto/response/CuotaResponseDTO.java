package com.loanmanager.backend.dto.response;
 
import com.loanmanager.backend.entity.Cuota;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaResponseDTO {
 
    private Long    id;
    private Integer numeroCuota;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal valorTotal;
    private LocalDate  fechaVencimiento;
    private BigDecimal saldoCapital;
    private Cuota.EstadoCuota estado;
 
    // Campos calculados — útiles para la UI
    private Boolean   estaVencida;
    private Long      diasMora;
    private BigDecimal interesMoraEstimado; // calculado en tiempo real
}