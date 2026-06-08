package com.loanmanager.backend.repository;
 
import com.loanmanager.backend.entity.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Long> {
 
    List<Cuota> findByPrestamoIdOrderByNumeroCuotaAsc(Long prestamoId);
 
    Optional<Cuota> findByPrestamoIdAndNumeroCuota(Long prestamoId,
                                                     Integer numeroCuota);
 
    // Cuotas vencidas sin pagar — usado por el job de detección de mora
    @Query("""
        SELECT c FROM Cuota c
        WHERE c.estado IN ('PENDIENTE', 'PAGADA_PARCIAL')
        AND c.fechaVencimiento < :hoy
        """)
    List<Cuota> findCuotasVencidas(@Param("hoy") LocalDate hoy);
 
    // Próximas cuotas a vencer (alerta temprana)
    @Query("""
        SELECT c FROM Cuota c
        WHERE c.estado = 'PENDIENTE'
        AND c.fechaVencimiento BETWEEN :desde AND :hasta
        ORDER BY c.fechaVencimiento ASC
        """)
    List<Cuota> findProximasAVencer(@Param("desde") LocalDate desde,
                                     @Param("hasta") LocalDate hasta);
 
    // Cuotas pendientes de un préstamo específico
    List<Cuota> findByPrestamoIdAndEstadoIn(Long prestamoId,
                                             List<Cuota.EstadoCuota> estados);
}
