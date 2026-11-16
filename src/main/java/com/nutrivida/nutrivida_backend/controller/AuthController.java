package com.nutrivida.nutrivida_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nutrivida.nutrivida_backend.dto.RegistroRequest;
import com.nutrivida.nutrivida_backend.dto.UsuarioResponse;
import com.nutrivida.nutrivida_backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
}
