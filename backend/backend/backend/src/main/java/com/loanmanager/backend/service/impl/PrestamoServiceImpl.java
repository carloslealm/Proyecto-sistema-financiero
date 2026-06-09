package com.loanmanager.backend.service.impl;
 
import com.loanmanager.backend.dto.request.PrestamoRequestDTO;
import com.loanmanager.backend.dto.response.*;
import com.loanmanager.backend.entity.Cliente;
import com.loanmanager.backend.entity.Cuota;
import com.loanmanager.backend.entity.Prestamo;
import com.loanmanager.backend.entity.Usuario;
import com.loanmanager.backend.exception.BadRequestException;
import com.loanmanager.backend.exception.BusinessException;
import com.loanmanager.backend.exception.ResourceNotFoundException;
import com.loanmanager.backend.repository.*;
import com.loanmanager.backend.service.PrestamoService;
import com.loanmanager.backend.util.AmortizacionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
 
@Slf4j
@Service
@RequiredArgsConstructor
public class PrestamoServiceImpl implements PrestamoService {
 
    private final PrestamoRepository  prestamoRepository;
    private final ClienteRepository   clienteRepository;
    private final UsuarioRepository   usuarioRepository;
    private final CuotaRepository     cuotaRepository;
    private final PagoRepository      pagoRepository;
    private final AmortizacionUtil    amortizacionUtil;
 
    @Override
    @Transactional(readOnly = true)
    public Page<PrestamoResponseDTO> listar(Pageable pageable) {
        return prestamoRepository.findAll(pageable)
            .map(this::toResponseDTO);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<PrestamoResponseDTO> listarPorEstado(String estado, Pageable pageable) {
        Prestamo.EstadoPrestamo estadoEnum;
        try {
            estadoEnum = Prestamo.EstadoPrestamo.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + estado);
        }
        return prestamoRepository.findByEstado(estadoEnum, pageable)
            .map(this::toResponseDTO);
    }
 
    @Override
    @Transactional(readOnly = true)
    public PrestamoResponseDTO obtenerPorId(Long id) {
        Prestamo prestamo = buscarPrestamo(id);
        return toResponseDTO(prestamo);
    }
 
    // ── SOLICITAR préstamo ────────────────────────────────────
    // Estado inicial: SOLICITADO
    // El asesor crea la solicitud en nombre del cliente.
    @Override
    @Transactional
    public PrestamoResponseDTO solicitar(PrestamoRequestDTO request, Long asesorId) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
            .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));
 
        if (!cliente.getActivo()) {
            throw new BusinessException("No se puede crear un préstamo para un cliente inactivo");
        }
 
        Usuario asesor = usuarioRepository.findById(asesorId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", asesorId));
 
        Prestamo prestamo = Prestamo.builder()
            .cliente(cliente)
            .asesor(asesor)
            .monto(request.getMonto())
            .tasaInteres(request.getTasaInteres())
            .plazoMeses(request.getPlazoMeses())
            .sistemaAmortizacion(request.getSistemaAmortizacion())
            .estado(Prestamo.EstadoPrestamo.SOLICITADO)
            .fechaSolicitud(LocalDate.now())
            .observaciones(request.getObservaciones())
            .build();
 
        prestamo = prestamoRepository.save(prestamo);
        log.info("Préstamo solicitado: ID={}, Cliente={}, Monto={}",
                 prestamo.getId(), cliente.getNombreCompleto(), prestamo.getMonto());
 
        return toResponseDTO(prestamo);
    }
 
    // ── APROBAR préstamo ──────────────────────────────────────
    // Estado: SOLICITADO → APROBADO
    // Validamos que esté en estado correcto antes de aprobar.
    @Override
    @Transactional
    public PrestamoResponseDTO aprobar(Long id, Long asesorId) {
        Prestamo prestamo = buscarPrestamo(id);
 
        if (prestamo.getEstado() != Prestamo.EstadoPrestamo.SOLICITADO
            && prestamo.getEstado() != Prestamo.EstadoPrestamo.EN_REVISION) {
            throw new BusinessException(
                "Solo se pueden aprobar préstamos en estado SOLICITADO o EN_REVISION. " +
                "Estado actual: " + prestamo.getEstado()
            );
        }
 
        prestamo.setEstado(Prestamo.EstadoPrestamo.APROBADO);
        prestamo.setFechaAprobacion(LocalDate.now());
        prestamo = prestamoRepository.save(prestamo);
 
        log.info("Préstamo aprobado: ID={}", id);
        return toResponseDTO(prestamo);
    }
 
    // ── RECHAZAR préstamo ─────────────────────────────────────
    @Override
    @Transactional
    public PrestamoResponseDTO rechazar(Long id, String motivo) {
        Prestamo prestamo = buscarPrestamo(id);
 
        if (prestamo.getEstado() == Prestamo.EstadoPrestamo.CANCELADO
            || prestamo.getEstado() == Prestamo.EstadoPrestamo.RECHAZADO) {
            throw new BusinessException("Este préstamo ya fue finalizado");
        }
 
        prestamo.setEstado(Prestamo.EstadoPrestamo.RECHAZADO);
        prestamo.setObservaciones(
            (prestamo.getObservaciones() != null ? prestamo.getObservaciones() + " | " : "")
            + "RECHAZADO: " + motivo
        );
        prestamo = prestamoRepository.save(prestamo);
 
        log.info("Préstamo rechazado: ID={}, Motivo={}", id, motivo);
        return toResponseDTO(prestamo);
    }
 
    // ── DESEMBOLSAR préstamo ──────────────────────────────────
    // Estado: APROBADO → DESEMBOLSADO → AL_DIA
    // Aquí ocurre la magia: se genera el plan de pagos completo.
    @Override
    @Transactional
    public PrestamoResponseDTO desembolsar(Long id) {
        Prestamo prestamo = buscarPrestamo(id);
 
        if (prestamo.getEstado() != Prestamo.EstadoPrestamo.APROBADO) {
            throw new BusinessException(
                "Solo se pueden desembolsar préstamos APROBADOS. " +
                "Estado actual: " + prestamo.getEstado()
            );
        }
 
        // Fecha de desembolso = hoy
        prestamo.setFechaDesembolso(LocalDate.now());
        prestamo.setEstado(Prestamo.EstadoPrestamo.AL_DIA);
 
        // Generar el plan de pagos con la calculadora
        List<Cuota> cuotas = amortizacionUtil.generarPlanDePagos(prestamo);
 
        // Calcular y guardar los totales en el préstamo
        BigDecimal totalAPagar  = amortizacionUtil.calcularTotalAPagar(cuotas);
        BigDecimal totalInteres = amortizacionUtil.calcularTotalInteres(cuotas);
 
        prestamo.setTotalAPagar(totalAPagar);
        prestamo.setTotalInteres(totalInteres);
 
        // Guardar el préstamo primero (las cuotas necesitan el ID)
        prestamo = prestamoRepository.save(prestamo);
 
        // Guardar todas las cuotas en batch
        cuotaRepository.saveAll(cuotas);
 
        log.info("Préstamo desembolsado: ID={}, Cuotas={}, Total={}",
                 id, cuotas.size(), totalAPagar);
 
        return toResponseDTO(prestamo);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<CuotaResponseDTO> obtenerPlanDePagos(Long prestamoId) {
        buscarPrestamo(prestamoId); // Valida que existe
 
        return cuotaRepository
            .findByPrestamoIdOrderByNumeroCuotaAsc(prestamoId)
            .stream()
            .map(this::toCuotaResponseDTO)
            .collect(Collectors.toList());
    }
 
    // ── DASHBOARD ─────────────────────────────────────────────
   @Override
@Transactional(readOnly = true)
public DashboardResponseDTO obtenerDashboard() {
    LocalDate hoy = LocalDate.now();
    YearMonth mesActual = YearMonth.now();

    List<Object[]> resumenEstados = prestamoRepository.resumenCarteraPorEstado();

    BigDecimal carteraTotal  = BigDecimal.ZERO;
    BigDecimal carteraEnMora = BigDecimal.ZERO;
    Map<String, Long> prestamosPorEstado = new LinkedHashMap<>();
    long prestamosActivos = 0;
    long prestamosEnMora  = 0;

    for (Object[] fila : resumenEstados) {
        // El enum se convierte a String con name()
        String estadoStr = ((Prestamo.EstadoPrestamo) fila[0]).name();
        long   count     = ((Number) fila[1]).longValue();
        BigDecimal monto = new BigDecimal(fila[2].toString());

        prestamosPorEstado.put(estadoStr, count);

        if (estadoStr.equals("AL_DIA") || estadoStr.equals("EN_MORA")) {
            carteraTotal = carteraTotal.add(monto);
            prestamosActivos += count;
        }
        if (estadoStr.equals("EN_MORA")) {
            carteraEnMora = carteraEnMora.add(monto);
            prestamosEnMora = count;
        }
    }

    BigDecimal porcentajeMora = carteraTotal.compareTo(BigDecimal.ZERO) > 0
        ? carteraEnMora.divide(carteraTotal, 4, RoundingMode.HALF_UP)
                       .multiply(BigDecimal.valueOf(100))
                       .setScale(2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    BigDecimal recaudado = pagoRepository.totalRecaudadoEnMes(
        mesActual.getMonthValue(), mesActual.getYear()
    );

    long cuotasVencidas = cuotaRepository.findCuotasVencidas(hoy).size();
    long totalClientes  = clienteRepository.count();

    return DashboardResponseDTO.builder()
        .carteraTotal(carteraTotal)
        .carteraEnMora(carteraEnMora)
        .recaudadoEsteMes(recaudado != null ? recaudado : BigDecimal.ZERO)
        .porcentajeMora(porcentajeMora)
        .totalClientes(totalClientes)
        .totalPrestamosActivos(prestamosActivos)
        .totalPrestamosEnMora(prestamosEnMora)
        .cuotasVencidasHoy(cuotasVencidas)
        .prestamosPorEstado(prestamosPorEstado)
        .build();
}
 
    // ── JOB de detección de mora ──────────────────────────────
    // @Scheduled ejecuta este método automáticamente cada día.
    // Cron: "0 0 1 * * *" = a la 1:00 AM todos los días.
    // Busca cuotas vencidas y actualiza los préstamos a EN_MORA.
    @Scheduled(cron = "0 0 1 * * *")
    @Override
    @Transactional
    public void detectarYActualizarMora() {
        log.info("Iniciando detección de mora...");
        LocalDate hoy = LocalDate.now();
 
        List<Cuota> cuotasVencidas = cuotaRepository.findCuotasVencidas(hoy);
 
        for (Cuota cuota : cuotasVencidas) {
            cuota.setEstado(Cuota.EstadoCuota.EN_MORA);
            cuotaRepository.save(cuota);
 
            Prestamo prestamo = cuota.getPrestamo();
            if (prestamo.getEstado() == Prestamo.EstadoPrestamo.AL_DIA) {
                prestamo.setEstado(Prestamo.EstadoPrestamo.EN_MORA);
                prestamoRepository.save(prestamo);
            }
        }
 
        log.info("Detección de mora completada. Cuotas en mora: {}", cuotasVencidas.size());
    }
 
    // ── Métodos privados de utilidad ──────────────────────────
 
    private Prestamo buscarPrestamo(Long id) {
        return prestamoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prestamo", id));
    }
 
    private PrestamoResponseDTO toResponseDTO(Prestamo p) {
        List<Cuota> cuotas = p.getCuotas();
 
        long pagadas   = cuotas.stream().filter(c -> c.getEstado() == Cuota.EstadoCuota.PAGADA).count();
        long enMora    = cuotas.stream().filter(c -> c.getEstado() == Cuota.EstadoCuota.EN_MORA).count();
        long pendientes = cuotas.stream().filter(c ->
            c.getEstado() == Cuota.EstadoCuota.PENDIENTE ||
            c.getEstado() == Cuota.EstadoCuota.PAGADA_PARCIAL).count();
 
        BigDecimal saldo = cuotas.stream()
            .filter(c -> c.getEstado() != Cuota.EstadoCuota.PAGADA
                      && c.getEstado() != Cuota.EstadoCuota.CONDONADA)
            .map(Cuota::getValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
 
        return PrestamoResponseDTO.builder()
            .id(p.getId())
            .clienteId(p.getCliente().getId())
            .clienteNombre(p.getCliente().getNombreCompleto())
            .clienteCedula(p.getCliente().getCedula())
            .asesorId(p.getAsesor().getId())
            .asesorNombre(p.getAsesor().getNombreCompleto())
            .monto(p.getMonto())
            .tasaInteres(p.getTasaInteres())
            .plazoMeses(p.getPlazoMeses())
            .sistemaAmortizacion(p.getSistemaAmortizacion())
            .estado(p.getEstado())
            .fechaSolicitud(p.getFechaSolicitud())
            .fechaAprobacion(p.getFechaAprobacion())
            .fechaDesembolso(p.getFechaDesembolso())
            .fechaCancelacion(p.getFechaCancelacion())
            .totalAPagar(p.getTotalAPagar())
            .totalInteres(p.getTotalInteres())
            .observaciones(p.getObservaciones())
            .totalCuotas(cuotas.size())
            .cuotasPagadas((int) pagadas)
            .cuotasPendientes((int) pendientes)
            .cuotasEnMora((int) enMora)
            .saldoPendiente(saldo)
            .createdAt(p.getCreatedAt())
            .build();
    }
 
    private CuotaResponseDTO toCuotaResponseDTO(Cuota c) {
        boolean vencida = c.estaVencida();
        long diasMora   = c.getDiasMora();
 
        BigDecimal moraSugerida = BigDecimal.ZERO;
        if (diasMora > 0) {
            moraSugerida = amortizacionUtil.calcularInteresMora(
                c.getValorTotal(),
                c.getPrestamo().getTasaInteres(),
                diasMora
            );
        }
 
        return CuotaResponseDTO.builder()
            .id(c.getId())
            .numeroCuota(c.getNumeroCuota())
            .capital(c.getCapital())
            .interes(c.getInteres())
            .valorTotal(c.getValorTotal())
            .fechaVencimiento(c.getFechaVencimiento())
            .saldoCapital(c.getSaldoCapital())
            .estado(c.getEstado())
            .estaVencida(vencida)
            .diasMora(diasMora)
            .interesMoraEstimado(moraSugerida)
            .build();
    }
}