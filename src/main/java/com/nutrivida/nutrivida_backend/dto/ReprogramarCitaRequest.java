package com.nutrivida.nutrivida_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Nueva fecha y hora para una cita (el dueño o el ADMIN)
public record ReprogramarCitaRequest(
        @Schema(example = "2026-12-04")
        @NotBlank(message = "La fecha es obligatoria.")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha debe tener el formato AAAA-MM-DD.")
        String fecha,

        @Schema(example = "11:00")
        @NotBlank(message = "La hora es obligatoria.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora debe tener el formato HH:MM.")
        String hora) {
}
