package com.loanmanager.backend.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
 
    // KPIs principales
    private BigDecimal carteraTotal;        // Suma de todos los préstamos activos
    private BigDecimal carteraEnMora;       // Suma de préstamos en mora
    private BigDecimal recaudadoEsteMes;    // Pagos recibidos en el mes actual
    private BigDecimal porcentajeMora;      // carteraEnMora / carteraTotal × 100
 
    // Conteos
    private Long totalClientes;
    private Long totalPrestamosActivos;
    private Long totalPrestamosEnMora;
    private Long cuotasVencidasHoy;
 
    // Para las gráficas del dashboard
    private Map<String, Long>       prestamosPorEstado;    // {AL_DIA: 45, EN_MORA: 8}
    private Map<String, BigDecimal> carteraPorMes;         // {Ene: 15M, Feb: 18M}
    private List<PrestamoResponseDTO> prestamosRecientes;  // Últimos 5
}