package com.nutrivida.nutrivida_backend.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

// Convierte las excepciones en respuestas JSON con el estado HTTP correcto
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> negocio(ApiException ex, HttpServletRequest req) {
        return responder(ex.getEstado(), ex.getMessage(), req);
    }

    // 400: datos que no cumplen las validaciones (@NotBlank, @Email, @RutValido...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        HttpStatus estado = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(estado).body(new ErrorResponse(estado.value(), estado.getReasonPhrase(),
                "Los datos enviados no son válidos.", errores, req.getRequestURI(), Instant.now()));
    }

    // 400: JSON mal formado o parámetro con tipo incorrecto (por ejemplo /usuarios/abc)
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    ResponseEntity<ErrorResponse> malFormado(Exception ex, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "La solicitud está mal formada.", req);
    }

    // 403: el usuario tiene un token válido, pero su rol no le permite la acción (@PreAuthorize)
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> sinPermiso(AccessDeniedException ex, HttpServletRequest req) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción.", req);
    }

    // 409: la base de datos rechazó el cambio por una restricción (por ejemplo, correo duplicado)
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, "El cambio entra en conflicto con datos existentes.", req);
    }

    // 500: error no previsto. Se registra en el log sin mostrar detalles al cliente.
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno.", req);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(estado)
                .body(ErrorResponse.de(estado.value(), estado.getReasonPhrase(), mensaje, req.getRequestURI()));
    }
}
