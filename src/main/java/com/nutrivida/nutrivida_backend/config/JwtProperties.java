package com.nutrivida.nutrivida_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Datos del JWT leídos desde application.properties (app.jwt.*)
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long duracionMinutos, String emisor, String destinatario) {

    public JwtProperties {
        // Sin una clave segura el backend no arranca (HS256 necesita al menos 256 bits)
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "Falta la variable de entorno JWT_SECRET (mínimo 32 caracteres).");
        }
    }
}
