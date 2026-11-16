package com.nutrivida.nutrivida_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// Datos que envía el formulario de inicio de sesión (LoginPage)
public record LoginRequest(

        @Schema(example = "camila.rojas@correo.cl")
        @NotBlank(message = "El correo es obligatorio.")
        String email,

        @Schema(example = "Clave-segura-2026")
        @NotBlank(message = "La contraseña es obligatoria.")
        String password) {
}
