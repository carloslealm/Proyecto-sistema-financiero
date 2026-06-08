package com.loanmanager.backend.controller;
 
import com.loanmanager.backend.dto.request.PrestamoRequestDTO;
import com.loanmanager.backend.dto.response.ApiResponseDTO;
import com.loanmanager.backend.dto.response.CuotaResponseDTO;
import com.loanmanager.backend.dto.response.PrestamoResponseDTO;
import com.loanmanager.backend.entity.Usuario;
import com.loanmanager.backend.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/prestamos")
@RequiredArgsConstructor
@Tag(name = "Préstamos", description = "Gestión del ciclo de vida de préstamos")
@SecurityRequirement(name = "bearerAuth")
public class PrestamoController {
 
    private final PrestamoService prestamoService;
 
    @GetMapping
    @Operation(summary = "Listar todos los préstamos con paginación")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<Page<PrestamoResponseDTO>>> listar(
            @RequestParam(defaultValue = "0")       int page,
            @RequestParam(defaultValue = "10")      int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
 
        Pageable pageable = PageRequest.of(page, size,
            Sort.by(sort).descending());
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Préstamos obtenidos", prestamoService.listar(pageable))
        );
    }
 
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar préstamos por estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<Page<PrestamoResponseDTO>>> listarPorEstado(
            @PathVariable String estado,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
 
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Préstamos por estado",
                prestamoService.listarPorEstado(estado, pageable))
        );
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Obtener préstamo por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<PrestamoResponseDTO>> obtener(
            @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Préstamo encontrado", prestamoService.obtenerPorId(id))
        );
    }
 
    @PostMapping
    @Operation(summary = "Solicitar nuevo préstamo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<PrestamoResponseDTO>> solicitar(
            @Valid @RequestBody PrestamoRequestDTO request,
            // @AuthenticationPrincipal inyecta el usuario autenticado
            // del SecurityContext — así sabemos qué asesor hizo la solicitud
            @AuthenticationPrincipal Usuario asesor) {
 
        PrestamoResponseDTO response = prestamoService.solicitar(request, asesor.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseDTO.ok("Préstamo solicitado exitosamente", response));
    }
 
    @PatchMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar préstamo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<PrestamoResponseDTO>> aprobar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario asesor) {
 
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Préstamo aprobado",
                prestamoService.aprobar(id, asesor.getId()))
        );
    }
 
    @PatchMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar préstamo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<PrestamoResponseDTO>> rechazar(
            @PathVariable Long id,
            @RequestParam String motivo) {
 
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Préstamo rechazado",
                prestamoService.rechazar(id, motivo))
        );
    }
 
    @PatchMapping("/{id}/desembolsar")
    @Operation(summary = "Desembolsar préstamo y generar plan de pagos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<PrestamoResponseDTO>> desembolsar(
            @PathVariable Long id) {
 
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Préstamo desembolsado y plan de pagos generado",
                prestamoService.desembolsar(id))
        );
    }
 
    @GetMapping("/{id}/plan-pagos")
    @Operation(summary = "Obtener plan de pagos completo del préstamo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<List<CuotaResponseDTO>>> planDePagos(
            @PathVariable Long id) {
 
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Plan de pagos",
                prestamoService.obtenerPlanDePagos(id))
        );
    }
}