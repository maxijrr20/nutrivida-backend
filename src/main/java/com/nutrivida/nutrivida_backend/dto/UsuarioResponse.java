package com.nutrivida.nutrivida_backend.dto;

import java.time.LocalDateTime;

import com.nutrivida.nutrivida_backend.model.Usuario;

// Datos de un usuario que devuelve la API. Nunca incluye la contraseña ni su hash.
public record UsuarioResponse(
        Integer id,
        String rut,
        String nombre,
        String email,
        String telefono,
        String rol,
        boolean activo,
        LocalDateTime creadoEn) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getRut(), u.getNombre(), u.getEmail(), u.getTelefono(),
                u.getRol().getNombre(), u.isActivo(), u.getCreadoEn());
    }
}
