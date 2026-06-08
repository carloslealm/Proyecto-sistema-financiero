package com.loanmanager.backend.util;
 
import com.loanmanager.backend.entity.Cuota;
import com.loanmanager.backend.entity.Prestamo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
@Slf4j
@Component
public class AmortizacionUtil {
 
    // Escala para operaciones internas — más decimales evitan
    // errores de redondeo acumulados en 36+ cuotas.
    private static final int ESCALA_CALCULO = 10;
    private static final int ESCALA_DINERO  = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
 
    /**
     * Genera el plan de pagos completo para un préstamo.
     * Punto de entrada principal — detecta el sistema y delega.
     */
    public List<Cuota> generarPlanDePagos(Prestamo prestamo) {
        return switch (prestamo.getSistemaAmortizacion()) {
            case FRANCES  -> calcularSistemaFrances(prestamo);
            case ALEMAN   -> calcularSistemaAleman(prestamo);
            case AMERICANO -> calcularSistemaAmericano(prestamo);
        };
    }
 
    // ── Sistema Francés ───────────────────────────────────────
    // Cuota fija, capital creciente, interés decreciente.
    // El más común en Colombia para préstamos de consumo.
 
    private List<Cuota> calcularSistemaFrances(Prestamo prestamo) {
        BigDecimal capital     = prestamo.getMonto();
        BigDecimal tasa        = prestamo.getTasaInteres();
        int        plazo       = prestamo.getPlazoMeses();
        LocalDate  fechaInicio = prestamo.getFechaDesembolso();
 
        // Fórmula de cuota fija del sistema francés:
        // C = P * i * (1+i)^n / ((1+i)^n - 1)
        BigDecimal unoPlusTasa = BigDecimal.ONE.add(tasa);
        BigDecimal potencia    = unoPlusTasa.pow(plazo, new MathContext(ESCALA_CALCULO));
 
        BigDecimal numerador   = capital.multiply(tasa).multiply(potencia)
                                        .setScale(ESCALA_CALCULO, REDONDEO);
        BigDecimal denominador = potencia.subtract(BigDecimal.ONE)
                                         .setScale(ESCALA_CALCULO, REDONDEO);
 
        BigDecimal cuotaFija   = numerador.divide(denominador, ESCALA_DINERO, REDONDEO);
 
        log.debug("Sistema Francés — Monto: {}, Tasa: {}, Plazo: {}, Cuota: {}",
                  capital, tasa, plazo, cuotaFija);
 
        List<Cuota> cuotas = new ArrayList<>();
        BigDecimal saldo   = capital;
 
        for (int n = 1; n <= plazo; n++) {
            // Interés del período = saldo actual × tasa
            BigDecimal interesPeriodo = saldo.multiply(tasa)
                                             .setScale(ESCALA_DINERO, REDONDEO);
 
            // Capital del período = cuota fija - interés
            BigDecimal capitalPeriodo = cuotaFija.subtract(interesPeriodo)
                                                  .setScale(ESCALA_DINERO, REDONDEO);
 
            // En la última cuota ajustamos por redondeos acumulados
            if (n == plazo) {
                capitalPeriodo = saldo.setScale(ESCALA_DINERO, REDONDEO);
                cuotaFija = capitalPeriodo.add(interesPeriodo);
            }
 
            // Nuevo saldo después de esta cuota
            BigDecimal nuevoSaldo = saldo.subtract(capitalPeriodo)
                                         .setScale(ESCALA_DINERO, REDONDEO);
 
            Cuota cuota = Cuota.builder()
                .prestamo(prestamo)
                .numeroCuota(n)
                .capital(capitalPeriodo)
                .interes(interesPeriodo)
                .valorTotal(cuotaFija)
                .fechaVencimiento(fechaInicio.plusMonths(n))
                .saldoCapital(nuevoSaldo.max(BigDecimal.ZERO))
                .estado(Cuota.EstadoCuota.PENDIENTE)
                .build();
 
            cuotas.add(cuota);
            saldo = nuevoSaldo;
        }
 
        return cuotas;
    }
 
    // ── Sistema Alemán ────────────────────────────────────────
    // Capital fijo, cuota decreciente.
    // Más usado en créditos empresariales.
 
    private List<Cuota> calcularSistemaAleman(Prestamo prestamo) {
        BigDecimal capital     = prestamo.getMonto();
        BigDecimal tasa        = prestamo.getTasaInteres();
        int        plazo       = prestamo.getPlazoMeses();
        LocalDate  fechaInicio = prestamo.getFechaDesembolso();
 
        // Capital fijo por período
        BigDecimal capitalFijo = capital.divide(
            BigDecimal.valueOf(plazo), ESCALA_DINERO, REDONDEO
        );
 
        List<Cuota> cuotas = new ArrayList<>();
        BigDecimal saldo   = capital;
 
        for (int n = 1; n <= plazo; n++) {
            BigDecimal interesPeriodo = saldo.multiply(tasa)
                                             .setScale(ESCALA_DINERO, REDONDEO);
            BigDecimal cuotaTotal     = capitalFijo.add(interesPeriodo)
                                                    .setScale(ESCALA_DINERO, REDONDEO);
 
            BigDecimal capitalN = (n == plazo) ? saldo : capitalFijo;
            BigDecimal nuevoSaldo = saldo.subtract(capitalN)
                                         .setScale(ESCALA_DINERO, REDONDEO);
 
            Cuota cuota = Cuota.builder()
                .prestamo(prestamo)
                .numeroCuota(n)
                .capital(capitalN)
                .interes(interesPeriodo)
                .valorTotal(cuotaTotal)
                .fechaVencimiento(fechaInicio.plusMonths(n))
                .saldoCapital(nuevoSaldo.max(BigDecimal.ZERO))
                .estado(Cuota.EstadoCuota.PENDIENTE)
                .build();
 
            cuotas.add(cuota);
            saldo = nuevoSaldo;
        }
 
        return cuotas;
    }
 
    // ── Sistema Americano ─────────────────────────────────────
    // Solo intereses durante el plazo, capital al final.
    // Usado en bonos y algunos créditos corporativos.
 
    private List<Cuota> calcularSistemaAmericano(Prestamo prestamo) {
        BigDecimal capital     = prestamo.getMonto();
        BigDecimal tasa        = prestamo.getTasaInteres();
        int        plazo       = prestamo.getPlazoMeses();
        LocalDate  fechaInicio = prestamo.getFechaDesembolso();
 
        BigDecimal soloInteres = capital.multiply(tasa)
                                        .setScale(ESCALA_DINERO, REDONDEO);
 
        List<Cuota> cuotas = new ArrayList<>();
 
        for (int n = 1; n <= plazo; n++) {
            boolean esUltima       = (n == plazo);
            BigDecimal capitalN    = esUltima ? capital : BigDecimal.ZERO;
            BigDecimal cuotaTotal  = soloInteres.add(capitalN);
            BigDecimal saldoN      = esUltima ? BigDecimal.ZERO : capital;
 
            Cuota cuota = Cuota.builder()
                .prestamo(prestamo)
                .numeroCuota(n)
                .capital(capitalN)
                .interes(soloInteres)
                .valorTotal(cuotaTotal)
                .fechaVencimiento(fechaInicio.plusMonths(n))
                .saldoCapital(saldoN)
                .estado(Cuota.EstadoCuota.PENDIENTE)
                .build();
 
            cuotas.add(cuota);
        }
 
        return cuotas;
    }
 
    // ── Métodos de utilidad ───────────────────────────────────
 
    /**
     * Calcula el total a pagar sumando todas las cuotas.
     * Se llama después de generar el plan para guardar
     * el resumen en el préstamo.
     */
    public BigDecimal calcularTotalAPagar(List<Cuota> cuotas) {
        return cuotas.stream()
            .map(Cuota::getValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(ESCALA_DINERO, REDONDEO);
    }
 
    public BigDecimal calcularTotalInteres(List<Cuota> cuotas) {
        return cuotas.stream()
            .map(Cuota::getInteres)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(ESCALA_DINERO, REDONDEO);
    }
 
    /**
     * Calcula el interés de mora.
     * Fórmula: saldo_cuota × tasa_mora_diaria × días_mora
     * Tasa mora Colombia: generalmente 1.5× la tasa corriente.
     */
    public BigDecimal calcularInteresMora(BigDecimal valorCuota,
                                           BigDecimal tasaMensual,
                                           long diasMora) {
        if (diasMora <= 0) return BigDecimal.ZERO;
 
        // Tasa diaria de mora = tasa mensual × 1.5 / 30
        BigDecimal tasaDiariaMora = tasaMensual
            .multiply(new BigDecimal("1.5"))
            .divide(BigDecimal.valueOf(30), ESCALA_CALCULO, REDONDEO);
 
        return valorCuota
            .multiply(tasaDiariaMora)
            .multiply(BigDecimal.valueOf(diasMora))
            .setScale(ESCALA_DINERO, REDONDEO);
    }
}