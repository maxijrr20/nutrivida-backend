package com.nutrivida.nutrivida_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nutrivida.nutrivida_backend.config.OpenApiConfig;
import com.nutrivida.nutrivida_backend.dto.LoginRequest;
import com.nutrivida.nutrivida_backend.dto.LoginResponse;
import com.nutrivida.nutrivida_backend.dto.RegistroRequest;
import com.nutrivida.nutrivida_backend.dto.UsuarioResponse;
import com.nutrivida.nutrivida_backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Rutas HTTP de autenticación. El controlador solo recibe la solicitud,
// valida el cuerpo (@Valid) y delega la lógica en AuthService.
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y usuario actual")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar paciente", description = "Ruta pública. El servidor asigna el rol PACIENTE.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (RUT, correo, contraseña...)")
    @ApiResponse(responseCode = "409", description = "El correo o el RUT ya están registrados")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return authService.registrar(datos);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Ruta pública. Devuelve un JWT tipo Bearer con su vigencia.")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas: accessToken, tokenType y expiresIn")
    @ApiResponse(responseCode = "400", description = "Faltan el correo o la contraseña")
    @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos (mensaje genérico)")
    public LoginResponse login(@Valid @RequestBody LoginRequest datos) {
        return authService.login(datos);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.BEARER)
    @Operation(summary = "Usuario actual", description = "Devuelve los datos del dueño del token.")
    @ApiResponse(responseCode = "200", description = "Datos del usuario, sin contraseña ni hash")
    @ApiResponse(responseCode = "401", description = "Token ausente, alterado o vencido")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioResponse.de(authService.usuarioActual(jwt));
    }
}
