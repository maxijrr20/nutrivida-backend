package com.nutrivida.nutrivida_backend.dto;

// Respuesta de un login correcto: el token, su tipo, su vigencia en segundos y los datos del usuario
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UsuarioResponse usuario) {
}
