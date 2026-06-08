// Archivo: src/main/java/com/loanmanager/backend/config/SwaggerConfig.java
package com.loanmanager.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

// @OpenAPIDefinition: metadatos de la API que aparecen
// en la portada de Swagger UI.
@OpenAPIDefinition(
    info = @Info(
        title = "LoanManager API",
        version = "1.0.0",
        description = "API REST para sistema de gestión de préstamos. " +
                      "Autenticarse con el endpoint /auth/login " +
                      "y usar el token en el botón Authorize.",
        contact = @Contact(
            name = "Tu Nombre",
            email = "tu@email.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080/api/v1",
                description = "Servidor de desarrollo"),
        @Server(url = "https://api.loanmanager.com/api/v1",
                description = "Servidor de producción")
    }
)
// @SecurityScheme: define cómo se autentica en Swagger.
// Agrega el botón "Authorize" donde pegas tu JWT.
// name = "bearerAuth" coincide con @SecurityRequirement en Controllers.
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Pega aquí el token JWT obtenido en /auth/login " +
                  "(sin el prefijo 'Bearer ')"
)
@Configuration
public class SwaggerConfig {
    // La configuración es 100% por anotaciones.
    // No necesita métodos adicionales.
}