package com.loanmanager.backend.exception;
 
import com.loanmanager.backend.dto.response.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
 
import java.util.HashMap;
import java.util.Map;
 
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
 
    // Maneja errores de validación (@Valid en el Controller).
    // Cuando un DTO falla la validación, Spring lanza esta excepción.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidacion(
            MethodArgumentNotValidException ex) {
 
        // Recopilamos todos los errores de validación del request.
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });
 
        log.warn("Error de validación: {}", errores);
 
        return ResponseEntity
            .badRequest()
            .body(ApiResponseDTO.<Map<String, String>>builder()
                .success(false)
                .message("Error de validación en los datos enviados")
                .data(errores)
                .build());
    }
 
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(
            ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponseDTO.error(ex.getMessage()));
    }
 
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(
            BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
            .badRequest()
            .body(ApiResponseDTO.error(ex.getMessage()));
    }
 
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBusiness(
            BusinessException ex) {
        log.warn("Error de negocio: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponseDTO.error(ex.getMessage()));
    }
 
    // AccessDeniedException: el usuario está autenticado pero
    // no tiene permisos para ese recurso. HTTP 403 Forbidden.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAccesoDenegado(
            AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponseDTO.error("No tienes permisos para realizar esta acción"));
    }
 
    // Captura cualquier excepción no manejada arriba.
    // Es el "catch-all" — evita que errores internos
    // expongan stack traces al cliente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneral(Exception ex) {
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseDTO.error(
                "Error interno del servidor. Contacte al administrador."
            ));
    }
}