package com.loanmanager.backend.controller;
 
import com.loanmanager.backend.dto.request.LoginRequestDTO;
import com.loanmanager.backend.dto.request.RegistroRequestDTO;
import com.loanmanager.backend.dto.response.ApiResponseDTO;
import com.loanmanager.backend.dto.response.AuthResponseDTO;
import com.loanmanager.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
// @RestController = @Controller + @ResponseBody
// Significa que todos los métodos retornan datos (JSON),
// no vistas HTML. Spring serializa automáticamente a JSON.
@RestController
// @RequestMapping: prefijo de ruta para todos los endpoints.
// Con el context-path del yml, la ruta completa es:
// /api/v1/auth/login
@RequestMapping("/auth")
@RequiredArgsConstructor
// @Tag: documentación para Swagger
@Tag(name = "Autenticación", description = "Login y registro de usuarios")
public class AuthController {
 
    private final AuthService authService;
 
    // POST /api/v1/auth/login
    @PostMapping("/login")
    // @Operation: documentación del endpoint en Swagger
    @Operation(summary = "Iniciar sesión",
               description = "Autentica un usuario y retorna un token JWT")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(
            // @Valid activa las validaciones del DTO (@NotBlank, @Email, etc.)
            // Si alguna falla, GlobalExceptionHandler la captura.
            @Valid @RequestBody LoginRequestDTO request) {
 
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(
            ApiResponseDTO.ok("Login exitoso", response)
        );
    }
 
    // POST /api/v1/auth/registro
    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario",
               description = "Crea un nuevo usuario asesor en el sistema")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> registro(
            @Valid @RequestBody RegistroRequestDTO request) {
 
        AuthResponseDTO response = authService.registro(request);
        // HTTP 201 Created: código semánticamente correcto para creación
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponseDTO.ok("Usuario registrado exitosamente", response));
    }

    @GetMapping("/generate-hash")
public String generateHash(@RequestParam String password) {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10)
        .encode(password);
}
}