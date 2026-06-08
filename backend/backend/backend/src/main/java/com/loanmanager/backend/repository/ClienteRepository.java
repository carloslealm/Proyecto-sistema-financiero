package com.loanmanager.backend.repository;
 
import com.loanmanager.backend.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.Optional;
 
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
 
    Optional<Cliente> findByCedula(String cedula);
 
    boolean existsByCedula(String cedula);
 
    boolean existsByEmail(String email);
 
    // Page<Cliente> permite paginación automática.
    // Pageable lleva: número de página, tamaño y ordenamiento.
    // El frontend enviará: /clientes?page=0&size=10&sort=nombre
    Page<Cliente> findByActivoTrue(Pageable pageable);
 
    // Búsqueda avanzada con múltiples campos.
    // LOWER() hace la búsqueda case-insensitive.
    // CONCAT con % es equivalente a LIKE '%termino%'.
    @Query("""
        SELECT c FROM Cliente c
        WHERE c.activo = true
        AND (
            LOWER(c.nombre)   LIKE LOWER(CONCAT('%', :termino, '%')) OR
            LOWER(c.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR
            LOWER(c.cedula)   LIKE LOWER(CONCAT('%', :termino, '%')) OR
            LOWER(c.email)    LIKE LOWER(CONCAT('%', :termino, '%'))
        )
        """)
    Page<Cliente> buscarPorTermino(@Param("termino") String termino,
                                    Pageable pageable);
}