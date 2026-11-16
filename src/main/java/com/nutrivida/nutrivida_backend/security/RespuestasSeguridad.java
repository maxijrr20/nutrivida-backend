package com.nutrivida.nutrivida_backend.security;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Respuestas JSON cuando Spring Security rechaza una solicitud antes de llegar al controlador:
// 401 -> sin token, token alterado o vencido.   403 -> rol insuficiente.
@Component
public class RespuestasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {
        escribir(res, HttpStatus.UNAUTHORIZED,
                "Debes iniciar sesión: el token falta, no es válido o está vencido.", req.getRequestURI());
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
            throws IOException {
        escribir(res, HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción.", req.getRequestURI());
    }

    private void escribir(HttpServletResponse res, HttpStatus estado, String mensaje, String ruta)
            throws IOException {
        res.setStatus(estado.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(String.format(
                "{\"estado\":%d,\"error\":\"%s\",\"mensaje\":\"%s\",\"ruta\":\"%s\",\"fecha\":\"%s\"}",
                estado.value(), estado.getReasonPhrase(), mensaje, ruta, Instant.now()));
    }
}
