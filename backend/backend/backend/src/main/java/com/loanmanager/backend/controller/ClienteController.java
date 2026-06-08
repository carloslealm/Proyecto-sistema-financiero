package com.loanmanager.backend.controller;
 
import com.loanmanager.backend.dto.request.ClienteRequestDTO;
import com.loanmanager.backend.dto.response.ApiResponseDTO;
import com.loanmanager.backend.dto.response.ClienteResponseDTO;
import com.loanmanager.backend.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes")
// @SecurityRequirement: le dice a Swagger que este Controller
// requiere el header Authorization con el token JWT.
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {
 
    private final ClienteService clienteService;
 
    // GET /api/v1/clientes?page=0&size=10&sort=nombre&buscar=juan
    @GetMapping
    @Operation(summary = "Listar clientes con paginación y búsqueda")
    // @PreAuthorize: protección a nivel de método.
    // Más granular que la configuración global en SecurityConfig.
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<Page<ClienteResponseDTO>>> listar(
            // Parámetros de paginación con valores por defecto
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "nombre") String sort,
            // Parámetro de búsqueda opcional (puede ser null)
            @RequestParam(required = false)      String buscar) {
 
        // Pageable encapsula: qué página, cuántos por página, y orden.
        Pageable pageable = PageRequest.of(
            page, size, Sort.by(sort).ascending()
        );
 
        Page<ClienteResponseDTO> clientes =
            clienteService.listar(buscar, pageable);
 
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Clientes obtenidos exitosamente", clientes)
        );
    }
 
    // GET /api/v1/clientes/5
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<ClienteResponseDTO>> obtenerPorId(
            // @PathVariable extrae el {id} de la URL
            @PathVariable Long id) {
 
        ClienteResponseDTO cliente = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Cliente encontrado", cliente)
        );
    }
 
    // POST /api/v1/clientes
    @PostMapping
    @Operation(summary = "Crear nuevo cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<ClienteResponseDTO>> crear(
            @Valid @RequestBody ClienteRequestDTO request) {
 
        ClienteResponseDTO cliente = clienteService.crear(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponseDTO.ok("Cliente creado exitosamente", cliente));
    }
 
    // PUT /api/v1/clientes/5
    // PUT = reemplaza el recurso completo.
    // PATCH = actualiza solo los campos enviados.
    // Usamos PUT para actualización completa del perfil del cliente.
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public ResponseEntity<ApiResponseDTO<ClienteResponseDTO>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO request) {
 
        ClienteResponseDTO cliente = clienteService.actualizar(id, request);
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Cliente actualizado exitosamente", cliente)
        );
    }
 
    // DELETE /api/v1/clientes/5
    // Hacemos soft delete (activo = false), no borrado físico.
    // Los datos financieros NUNCA se borran — son auditables.
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar cliente (soft delete)")
    @PreAuthorize("hasRole('ADMIN')")  // Solo ADMIN puede desactivar
    public ResponseEntity<ApiResponseDTO<Void>> desactivar(
            @PathVariable Long id) {
 
        clienteService.desactivar(id);
        // HTTP 200 con body descriptivo — más informativo que 204 No Content
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Cliente desactivado exitosamente", null)
        );
    }
}