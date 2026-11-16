package com.nutrivida.nutrivida_backend.exception;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

// Formato común de los errores de la API
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int estado,
        String error,
        String mensaje,
        Map<String, String> errores, // errores por campo (solo en validaciones, 400)
        String ruta,
        Instant fecha) {

    public static ErrorResponse de(int estado, String error, String mensaje, String ruta) {
        return new ErrorResponse(estado, error, mensaje, null, ruta, Instant.now());
    }
}
