package com.nutrivida.nutrivida_backend.dto;

import com.nutrivida.nutrivida_backend.validation.RutValido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Alta de usuarios desde el panel de administración (POST /api/v1/usuarios, solo ADMIN).
// A diferencia del registro público, aquí el ADMIN elige el rol.
public record CrearUsuarioRequest(

        @Schema(example = "15.678.901-3")
        @NotBlank(message = "El RUT es obligatorio.")
        @RutValido
        String rut,

        @Schema(example = "Daniela Soto")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres.")
        String nombre,

        @Schema(example = "daniela.soto@nutrivida.cl")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 120, message = "El correo puede tener hasta 120 caracteres.")
        String email,

        @Schema(example = "+56987654321")
        @Pattern(regexp = "^$|^\\+?\\d{8,15}$", message = "El teléfono debe tener entre 8 y 15 dígitos.")
        String telefono,

        @Schema(example = "Clave-inicial-2026")
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        String password,

        @Schema(example = "NUTRICIONISTA", allowableValues = { "ADMIN", "NUTRICIONISTA", "PACIENTE" })
        @NotBlank(message = "El rol es obligatorio.")
        @Pattern(regexp = "ADMIN|NUTRICIONISTA|PACIENTE", message = "El rol debe ser ADMIN, NUTRICIONISTA o PACIENTE.")
        String rol) {
}
