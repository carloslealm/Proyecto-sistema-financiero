package com.loanmanager.backend.repository;
 
import com.loanmanager.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDateTime;
import java.util.Optional;
 
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
 
    // Usado en el login: buscar usuario por email
    Optional<Usuario> findByEmail(String email);
 
    // Verificar si el email ya existe (para registro)
    boolean existsByEmail(String email);
 
    // Usuarios activos solamente
    java.util.List<Usuario> findByActivoTrue();
 
    // @Query permite escribir JPQL (Java Persistence Query Language).
    // JPQL es como SQL pero usa nombres de CLASES y CAMPOS Java,
    // no nombres de tablas y columnas SQL.
    // "u" es alias de Usuario, "u.email" → campo email de la clase.
    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.activo = true")
    Optional<Usuario> findActivoByEmail(@Param("email") String email);
 
    // @Modifying + @Transactional: necesarios para queries de UPDATE/DELETE.
    // Sin @Transactional Spring lanza TransactionRequiredException.
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.ultimoAcceso = :fecha WHERE u.id = :id")
    void actualizarUltimoAcceso(@Param("id") Long id,
                                 @Param("fecha") LocalDateTime fecha);

}