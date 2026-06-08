// Archivo: src/main/java/com/loanmanager/backend/entity/Cliente.java
package com.loanmanager.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente extends BaseEntity {

    // @NotBlank: no puede ser null ni cadena vacía ni solo espacios.
    // @Size: valida longitud ANTES de llegar a la BD.
    // Validar en Java (capa de DTO/Service) Y en BD es redundante
    // pero correcto: la BD es el último recurso, Java es más rápido.
    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    // BigDecimal es el tipo correcto para dinero en Java.
    // Double y Float tienen errores de precisión (igual que en BD).
    // precision = 12: total de dígitos
    // scale = 2: dígitos decimales
    @Column(name = "ingreso_mensual", precision = 12, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // mappedBy = "cliente": la FK está en la tabla prestamo,
    // en el campo "cliente" de la entidad Prestamo.
    // cascade = ALL: si borras un cliente, se borran sus préstamos.
    // orphanRemoval = true: si quitas un préstamo de la lista,
    // Hibernate lo borra de la BD automáticamente.
    @OneToMany(mappedBy = "cliente",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prestamo> prestamos = new ArrayList<>();

    // Método de utilidad — muy útil en reportes y logs
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }
}