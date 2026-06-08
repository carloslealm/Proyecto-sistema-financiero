package com.loanmanager.backend.dto.response;
 
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
 
import java.time.LocalDateTime;
 
// @JsonInclude(NON_NULL): campos null no aparecen en el JSON.
// Si "data" es null (ej: en un error), no sale en la respuesta.
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {
 
    // true si la operación fue exitosa, false si hubo error.
    private boolean success;
 
    // Mensaje legible para el usuario o para debug.
    private String message;
 
    // Los datos de la respuesta (puede ser cualquier tipo).
    // En una lista de clientes sería List<ClienteResponseDTO>.
    // En un error sería null.
    private T data;
 
    // Timestamp para trazabilidad — cuándo ocurrió la respuesta.
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
 
    // Métodos factory — evitan construir el builder cada vez.
    public static <T> ApiResponseDTO<T> ok(String message, T data) {
        return ApiResponseDTO.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }
 
    public static <T> ApiResponseDTO<T> error(String message) {
        return ApiResponseDTO.<T>builder()
            .success(false)
            .message(message)
            .build();
    }
}