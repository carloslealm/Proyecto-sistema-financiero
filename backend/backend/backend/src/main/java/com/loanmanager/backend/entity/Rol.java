// Archivo: src/main/java/com/loanmanager/backend/entity/Rol.java
package com.loanmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
@Getter
@Setter
// @NoArgsConstructor genera: public Rol() {}
// JPA REQUIERE un constructor sin argumentos para poder
// instanciar los objetos cuando lee de la BD.
@NoArgsConstructor
// @AllArgsConstructor genera un constructor con todos los campos.
@AllArgsConstructor
// @Builder permite construir objetos así:
//   Rol rol = Rol.builder().nombre("ADMIN").build();
// Mucho más legible que setters encadenados.
@Builder
public class Rol extends BaseEntity {

    // unique = true crea un índice UNIQUE en la columna.
    // nullable = false → NOT NULL en BD.
    // length = 50 → VARCHAR(50).
    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // mappedBy = "roles" indica que la FK está en la tabla
    // usuario_rol, gestionada por el campo "roles" en Usuario.
    // fetch = LAZY: los usuarios NO se cargan hasta que
    // explícitamente los pidas. Esto evita queries gigantes.
    // EAGER cargaría todos los usuarios de un rol cada vez
    // que consultes el rol — un desastre de rendimiento.
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Usuario> usuarios = new HashSet<>();

    // Constructor útil para crear roles rápido en los seeders
    public Rol(String nombre) {
        this.nombre = nombre;
        this.activo = true;
    }
}