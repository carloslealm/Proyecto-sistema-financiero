// ─────────────────────────────────────────────────────────────
// Archivo: src/main/java/com/loanmanager/backend/entity/BaseEntity.java
//
// ¿Por qué una clase base?
// Principio DRY: en vez de declarar id, created_at y updated_at
// en cada entity (7 veces), los declaramos una vez aquí y todos
// los heredan. Si el día de mañana agrega un campo "deleted_at"
// para soft delete, solo lo cambia en UN lugar.
// ─────────────────────────────────────────────────────────────
package com.loanmanager.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

// @MappedSuperclass le dice a JPA:
// "Esta clase NO es una tabla. Es solo un molde para otras clases."
// Sus campos se incluirán en las tablas de las clases hijas.
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    // serialVersionUID es requerido por Serializable.
    // Garantiza compatibilidad cuando el objeto se serializa
    // (ej: se guarda en caché o se envía por red).
    private static final long serialVersionUID = 1L;

    @Id
    // IDENTITY le dice a Hibernate que confíe en el AUTO_INCREMENT
    // de MySQL para generar el ID. Alternativas:
    //   SEQUENCE → usa secuencias (Oracle, PostgreSQL)
    //   AUTO     → Hibernate decide (no recomendado, impredecible)
    //   TABLE    → usa una tabla auxiliar para IDs (obsoleto)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @CreationTimestamp: Hibernate asigna la fecha/hora actual
    // SOLO cuando se hace el INSERT. Nunca cambia después.
    // updatable = false garantiza que no se modifique en UPDATEs.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @UpdateTimestamp: Hibernate actualiza este campo
    // automáticamente en cada UPDATE. Muy útil para auditoría.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}