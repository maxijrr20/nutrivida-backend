package com.nutrivida.nutrivida_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Crear o renombrar una categoría (solo ADMIN)
public record CategoriaRequest(
        @Schema(example = "Talleres")
        @NotBlank(message = "El nombre de la categoría es obligatorio.")
        @Size(max = 60, message = "El nombre puede tener hasta 60 caracteres.")
        String nombre) {
}
