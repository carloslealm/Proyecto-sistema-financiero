package com.loanmanager.backend.repository;
 
import com.loanmanager.backend.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.Optional;
 
// JpaRepository<Rol, Long> significa:
//   - Entidad que maneja: Rol
//   - Tipo del ID: Long
// Al heredar JpaRepository obtienes GRATIS:
//   save(), findById(), findAll(), deleteById(),
//   count(), existsById(), y muchos más.
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
 
    // Spring genera: SELECT * FROM rol WHERE nombre = ?
    Optional<Rol> findByNombre(String nombre);
 
    // Spring genera: SELECT * FROM rol WHERE activo = ?
    java.util.List<Rol> findByActivoTrue();
}