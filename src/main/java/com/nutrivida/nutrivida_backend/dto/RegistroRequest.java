package com.nutrivida.nutrivida_backend.dto;

import com.nutrivida.nutrivida_backend.validation.RutValido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Datos que envía el formulario de registro (RegistroPage).
// No trae "rol": el servidor asigna siempre PACIENTE, nadie puede registrarse como administrador.
public record RegistroRequest(

        @Schema(example = "12.345.678-5")
        @NotBlank(message = "El RUT es obligatorio.")
        @RutValido
        String rut,

        @Schema(example = "Camila Rojas")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres.")
        String nombre,

        @Schema(example = "camila.rojas@correo.cl")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 120, message = "El correo puede tener hasta 120 caracteres.")
        String email,

        @Schema(example = "+56912345678")
        @Pattern(regexp = "^$|^\\+?\\d{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos.")
        String telefono,

        @Schema(example = "Clave-segura-2026")
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        String password) {
}
