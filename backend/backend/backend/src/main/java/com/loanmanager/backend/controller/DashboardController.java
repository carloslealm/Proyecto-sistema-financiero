package com.loanmanager.backend.controller;
 
import com.loanmanager.backend.dto.response.ApiResponseDTO;
import com.loanmanager.backend.dto.response.DashboardResponseDTO;
import com.loanmanager.backend.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "KPIs y métricas del sistema")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {
 
    private final PrestamoService prestamoService;
 
    @GetMapping
    @Operation(summary = "Obtener métricas generales del dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<DashboardResponseDTO>> dashboard() {
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Dashboard obtenido",
                prestamoService.obtenerDashboard())
        );
    }
}