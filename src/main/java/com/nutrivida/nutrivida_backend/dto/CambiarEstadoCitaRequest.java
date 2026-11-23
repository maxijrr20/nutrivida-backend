package com.nutrivida.nutrivida_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Cambio de estado de una solicitud por el ADMIN
public record CambiarEstadoCitaRequest(
        @Schema(example = "Confirmada", allowableValues = { "Pendiente", "Confirmada", "Reprogramada", "Cancelada", "Rechazada" })
        @NotBlank(message = "El estado es obligatorio.")
        @Pattern(regexp = "Pendiente|Confirmada|Reprogramada|Cancelada|Rechazada",
                message = "El estado debe ser Pendiente, Confirmada, Reprogramada, Cancelada o Rechazada.")
        String estado) {
}
