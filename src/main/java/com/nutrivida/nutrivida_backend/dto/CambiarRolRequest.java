package com.nutrivida.nutrivida_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Cuerpo de PATCH /api/v1/usuarios/{id}/rol (solo ADMIN)
public record CambiarRolRequest(

        @Schema(example = "NUTRICIONISTA", allowableValues = { "ADMIN", "NUTRICIONISTA", "PACIENTE" })
        @NotBlank(message = "El rol es obligatorio.")
        @Pattern(regexp = "ADMIN|NUTRICIONISTA|PACIENTE", message = "El rol debe ser ADMIN, NUTRICIONISTA o PACIENTE.")
        String rol) {
}
