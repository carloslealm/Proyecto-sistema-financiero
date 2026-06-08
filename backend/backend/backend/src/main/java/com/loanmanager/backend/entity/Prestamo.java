// Archivo: src/main/java/com/loanmanager/backend/entity/Prestamo.java
package com.loanmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prestamo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestamo extends BaseEntity {

    // ── Enums internos ───────────────────────────────────────
    // Definir los enums dentro del entity que los usa es una
    // buena práctica cuando solo ese entity los usa.
    // Si varios entities los usaran, irían en su propio archivo.

    public enum SistemaAmortizacion {
        FRANCES,    // Cuota fija, capital creciente, interés decreciente
        ALEMAN,     // Capital fijo, cuota decreciente
        AMERICANO   // Solo intereses, capital al final
    }

    public enum EstadoPrestamo {
        SOLICITADO,    // Recién ingresado al sistema
        EN_REVISION,   // El asesor lo está analizando
        APROBADO,      // Aprobado, pendiente de desembolso
        RECHAZADO,     // No cumplió los criterios
        DESEMBOLSADO,  // Dinero entregado al cliente
        AL_DIA,        // Vigente y sin mora
        EN_MORA,       // Tiene cuotas vencidas sin pagar
        CANCELADO,     // Pagado completamente
        CASTIGADO      // Irrecuperable, dado de baja contablemente
    }

    // ── Relaciones ───────────────────────────────────────────

    // @ManyToOne: muchos préstamos → un cliente.
    // fetch = LAZY: no cargar el cliente automáticamente.
    // optional = false: un préstamo SIEMPRE tiene un cliente (NOT NULL).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asesor_id", nullable = false)
    private Usuario asesor;

    // ── Condiciones financieras ───────────────────────────────

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    // 0.0245 = 2.45% mensual
    // precision = 5, scale = 4 permite valores como 0.2450
    @Column(name = "tasa_interes", nullable = false, precision = 5, scale = 4)
    private BigDecimal tasaInteres;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    // EnumType.STRING guarda "FRANCES" en vez de 0, 1, 2.
    // NUNCA uses EnumType.ORDINAL: si reordenas el enum,
    // todos los datos históricos quedan incorrectos.
    @Enumerated(EnumType.STRING)
    @Column(name = "sistema_amortizacion", nullable = false, length = 20)
    @Builder.Default
    private SistemaAmortizacion sistemaAmortizacion = SistemaAmortizacion.FRANCES;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPrestamo estado = EstadoPrestamo.SOLICITADO;

    // ── Fechas del ciclo de vida ──────────────────────────────

    @Column(name = "fecha_solicitud", nullable = false)
    @Builder.Default
    private LocalDate fechaSolicitud = LocalDate.now();

    @Column(name = "fecha_aprobacion")
    private LocalDate fechaAprobacion;

    @Column(name = "fecha_desembolso")
    private LocalDate fechaDesembolso;

    @Column(name = "fecha_cancelacion")
    private LocalDate fechaCancelacion;

    // ── Totales calculados ────────────────────────────────────
    // Se guardan al momento del desembolso para trazabilidad.
    // Ver explicación en el script SQL (Sección 3, tabla prestamo).

    @Column(name = "total_a_pagar", precision = 14, scale = 2)
    private BigDecimal totalAPagar;

    @Column(name = "total_interes", precision = 14, scale = 2)
    private BigDecimal totalInteres;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // ── Cuotas ───────────────────────────────────────────────

    @OneToMany(mappedBy = "prestamo",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<Cuota> cuotas = new ArrayList<>();

    // ── Métodos de utilidad ───────────────────────────────────

    public boolean estaEnMora() {
        return this.estado == EstadoPrestamo.EN_MORA;
    }

    public boolean estaVigente() {
        return this.estado == EstadoPrestamo.AL_DIA
            || this.estado == EstadoPrestamo.EN_MORA
            || this.estado == EstadoPrestamo.DESEMBOLSADO;
    }
}