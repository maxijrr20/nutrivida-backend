package com.nutrivida.nutrivida_backend.dto;

import com.nutrivida.nutrivida_backend.validation.RutValido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Nueva solicitud de cita (cualquier usuario con sesión). La cuenta que la crea queda como dueña.
public record CitaRequest(
        @Schema(example = "Camila Rojas")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres.")
        String nombre,

        @Schema(example = "12.345.678-5")
        @NotBlank(message = "El RUT es obligatorio.")
        @RutValido
        String rut,

        @Schema(example = "camila.rojas@correo.cl")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 120, message = "El correo puede tener hasta 120 caracteres.")
        String email,

        @Schema(example = "+56912345678")
        @Pattern(regexp = "^$|^\\+?\\d{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos.")
        String telefono,

        @Schema(example = "1")
        @NotNull(message = "Selecciona un servicio.")
        Integer servicioId,

        @Schema(example = "NUT001")
        @NotBlank(message = "Selecciona un nutricionista.")
        String nutricionistaCodigo,

        @Schema(example = "2026-12-02")
        @NotBlank(message = "La fecha es obligatoria.")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha debe tener el formato AAAA-MM-DD.")
        String fecha,

        @Schema(example = "10:30")
        @NotBlank(message = "La hora es obligatoria.")
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora debe tener el formato HH:MM.")
        String hora,

        @Schema(example = "Quiero bajar de peso y mejorar mis exámenes.")
        @Size(max = 500, message = "El motivo puede tener hasta 500 caracteres.")
        String motivo) {
}
