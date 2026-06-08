package com.loanmanager.backend.repository;
 
import com.loanmanager.backend.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
 
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
 
    List<Pago> findByCuotaId(Long cuotaId);
 
    // Total pagado en una cuota (para calcular si está completa)
    @Query("SELECT COALESCE(SUM(p.montoPagado), 0) FROM Pago p WHERE p.cuota.id = :cuotaId")
    BigDecimal totalPagadoPorCuota(@Param("cuotaId") Long cuotaId);
 
    // Pagos en un rango de fechas — para reportes mensuales
    @Query("""
        SELECT p FROM Pago p
        WHERE p.fechaPago BETWEEN :desde AND :hasta
        ORDER BY p.fechaPago DESC
        """)
    List<Pago> findPagosPorPeriodo(@Param("desde") LocalDate desde,
                                    @Param("hasta") LocalDate hasta);
 
    // Total recaudado en el mes — KPI del dashboard
    @Query("""
        SELECT COALESCE(SUM(p.montoTotal), 0) FROM Pago p
        WHERE MONTH(p.fechaPago) = :mes AND YEAR(p.fechaPago) = :anio
        """)
    BigDecimal totalRecaudadoEnMes(@Param("mes") int mes,
                                    @Param("anio") int anio);
}