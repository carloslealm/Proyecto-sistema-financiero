package com.loanmanager.backend.dto.response;
 
import com.loanmanager.backend.entity.Prestamo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoResponseDTO {
 
    private Long   id;
    private Long   clienteId;
    private String clienteNombre;
    private String clienteCedula;
    private Long   asesorId;
    private String asesorNombre;
 
    private BigDecimal monto;
    private BigDecimal tasaInteres;
    private Integer    plazoMeses;
    private Prestamo.SistemaAmortizacion sistemaAmortizacion;
    private Prestamo.EstadoPrestamo      estado;
 
    private LocalDate fechaSolicitud;
    private LocalDate fechaAprobacion;
    private LocalDate fechaDesembolso;
    private LocalDate fechaCancelacion;
 
    private BigDecimal totalAPagar;
    private BigDecimal totalInteres;
    private String     observaciones;
 
    // Resumen de cuotas — no incluimos todas para no sobrecargar
    private Integer totalCuotas;
    private Integer cuotasPagadas;
    private Integer cuotasPendientes;
    private Integer cuotasEnMora;
    private BigDecimal saldoPendiente;
 
    // El plan completo solo se incluye cuando se solicita
    // explícitamente (endpoint /prestamos/{id}/plan-pagos)
    private List<CuotaResponseDTO> planDePagos;
 
    private LocalDateTime createdAt;
}