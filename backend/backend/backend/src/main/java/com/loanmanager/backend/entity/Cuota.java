package com.loanmanager.backend.entity;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
@Entity
@Table(name = "cuota")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuota extends BaseEntity {
 
    public enum EstadoCuota {
        PENDIENTE,
        PAGADA,
        PAGADA_PARCIAL,
        EN_MORA,
        CONDONADA
    }
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestamo_id", nullable = false)
    private Prestamo prestamo;
 
    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;
 
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capital;
 
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interes;
 
    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;
 
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;
 
    @Column(name = "saldo_capital", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoCapital;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCuota estado = EstadoCuota.PENDIENTE;
 
    @OneToMany(mappedBy = "cuota",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<Pago> pagos = new ArrayList<>();
 
    // ── Métodos de utilidad ───────────────────────────────────
 
    public boolean estaVencida() {
        return this.estado == EstadoCuota.PENDIENTE
            && LocalDate.now().isAfter(this.fechaVencimiento);
    }
 
    public long getDiasMora() {
        if (!estaVencida()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(
            this.fechaVencimiento, LocalDate.now()
        );
    }
}