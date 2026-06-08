// Archivo: src/main/java/com/loanmanager/backend/entity/Pago.java
package com.loanmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago extends BaseEntity {

    public enum MedioPago {
        EFECTIVO, TRANSFERENCIA, CONSIGNACION,
        CHEQUE, TARJETA, PSE, OTRO
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuota_id", nullable = false)
    private Cuota cuota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;

    @Column(name = "monto_pagado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoPagado;

    // Tu decisión del reto: campo de mora en el pago.
    // BigDecimal.ZERO es la constante correcta para 0.00 en dinero.
    // Nunca uses new BigDecimal(0) — es menos eficiente.
    @Column(name = "interes_mora", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal interesMora = BigDecimal.ZERO;

    @Column(name = "dias_mora", nullable = false)
    @Builder.Default
    private Integer diasMora = 0;

    // monto_total = monto_pagado + interes_mora
    // Lo guardamos para evitar recalcularlo en cada consulta.
    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "fecha_pago", nullable = false)
    @Builder.Default
    private LocalDate fechaPago = LocalDate.now();

    @Column(length = 100)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false, length = 20)
    @Builder.Default
    private MedioPago medioPago = MedioPago.EFECTIVO;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}