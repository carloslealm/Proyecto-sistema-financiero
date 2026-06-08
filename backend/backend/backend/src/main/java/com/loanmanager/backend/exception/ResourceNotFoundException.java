package com.loanmanager.backend.exception;
 
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
 
// @ResponseStatus: cuando esta excepción se lanza y no es capturada,
// Spring automáticamente responde con HTTP 404.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " no encontrado con id: " + id);
    }
}