package com.loanmanager.backend.repository;
 
import com.loanmanager.backend.entity.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.math.BigDecimal;
import java.util.List;
 
@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
 
    // Préstamos de un cliente específico
    List<Prestamo> findByClienteId(Long clienteId);
 
    // Préstamos gestionados por un asesor
    Page<Prestamo> findByAsesorId(Long asesorId, Pageable pageable);
 
    // Préstamos por estado con paginación
    Page<Prestamo> findByEstado(Prestamo.EstadoPrestamo estado, Pageable pageable);
 
    // Consulta para el dashboard: cartera total por estado
    @Query("""
        SELECT p.estado, COUNT(p), SUM(p.monto)
        FROM Prestamo p
        GROUP BY p.estado
        """)
    List<Object[]> resumenCarteraPorEstado();
 
    // Top asesores por monto desembolsado — responde el reto de la Fase 2
    @Query("""
        SELECT u.nombre, u.apellido, COUNT(p), SUM(p.monto),
               SUM(CASE WHEN p.estado = 'EN_MORA' THEN 1 ELSE 0 END)
        FROM Prestamo p
        JOIN p.asesor u
        WHERE p.estado NOT IN ('SOLICITADO', 'RECHAZADO')
        GROUP BY u.id, u.nombre, u.apellido
        ORDER BY SUM(p.monto) DESC
        """)
    List<Object[]> topAsesoresPorMonto(Pageable pageable);
}