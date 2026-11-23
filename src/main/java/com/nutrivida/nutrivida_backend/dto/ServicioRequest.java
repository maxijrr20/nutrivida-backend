package com.nutrivida.nutrivida_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Crear o modificar un servicio del catálogo (solo ADMIN).
// La categoría se indica por su nombre, igual que en el panel del frontend.
public record ServicioRequest(
        @Schema(example = "Control nutricional online")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre puede tener hasta 120 caracteres.")
        String nombre,

        @Schema(example = "Consultas")
        @NotBlank(message = "La categoría es obligatoria.")
        String categoria,

        @Schema(example = "Consulta")
        @Size(max = 40, message = "El tipo puede tener hasta 40 caracteres.")
        String tipo,

        @Schema(example = "30 min")
        @Size(max = 30, message = "La duración puede tener hasta 30 caracteres.")
        String duracion,

        @Schema(example = "Online (video)")
        @Size(max = 40, message = "La modalidad puede tener hasta 40 caracteres.")
        String modalidad,

        @Schema(example = "22000")
        @NotNull(message = "El precio es obligatorio.")
        @Positive(message = "El precio debe ser mayor a 0.")
        @Max(value = 10000000, message = "El precio es demasiado alto.")
        Integer precio,

        @Schema(example = "Seguimiento por videollamada con ajuste del plan.")
        @Size(max = 1000, message = "La descripción puede tener hasta 1000 caracteres.")
        String descripcion) {
}
