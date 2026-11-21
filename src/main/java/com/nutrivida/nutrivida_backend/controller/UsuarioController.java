package com.nutrivida.nutrivida_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nutrivida.nutrivida_backend.config.OpenApiConfig;
import com.nutrivida.nutrivida_backend.dto.ActualizarUsuarioRequest;
import com.nutrivida.nutrivida_backend.dto.CambiarRolRequest;
import com.nutrivida.nutrivida_backend.dto.CrearUsuarioRequest;
import com.nutrivida.nutrivida_backend.dto.UsuarioResponse;
import com.nutrivida.nutrivida_backend.service.AuthService;
import com.nutrivida.nutrivida_backend.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Usuarios: listar, ver, modificar y desactivar. Todas las rutas exigen JWT (401 sin token).
// @PreAuthorize("hasRole('ADMIN')") deja pasar solo al administrador (403 para otros roles).
@RestController
@RequestMapping("/api/v1/usuarios")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@Tag(name = "Usuarios", description = "Crear, listar, ver, modificar, cambiar rol y desactivar usuarios")
@ApiResponse(responseCode = "401", description = "Token ausente, alterado o vencido")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear usuario con rol", description = "Solo ADMIN. Permite crear nutricionistas, pacientes u otros administradores.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (RUT, correo, contraseña, rol...)")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "409", description = "El correo o el RUT ya están registrados")
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest datos) {
        return usuarioService.crear(datos);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios", description = "Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un usuario", description = "ADMIN ve cualquiera; los demás solo su propia cuenta.")
    @ApiResponse(responseCode = "200", description = "Datos del usuario")
    @ApiResponse(responseCode = "403", description = "Intenta ver la cuenta de otra persona")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    public UsuarioResponse obtener(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.obtener(id, authService.usuarioActual(jwt));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar usuario", description = "ADMIN o el mismo usuario. No cambia RUT, rol ni estado.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "403", description = "Intenta modificar la cuenta de otra persona")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    @ApiResponse(responseCode = "409", description = "El correo ya es de otra cuenta")
    public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ActualizarUsuarioRequest datos,
            @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.actualizar(id, datos, authService.usuarioActual(jwt));
    }

    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar rol", description = "Solo ADMIN. El rol inicial lo asigna el servidor; solo el administrador puede cambiarlo.")
    @ApiResponse(responseCode = "200", description = "Rol actualizado")
    @ApiResponse(responseCode = "400", description = "Rol inválido")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    @ApiResponse(responseCode = "409", description = "El admin intenta quitarse su propio rol")
    public UsuarioResponse cambiarRol(@PathVariable Integer id, @Valid @RequestBody CambiarRolRequest datos,
            @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.cambiarRol(id, datos, authService.usuarioActual(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar usuario", description = "Solo ADMIN. Eliminación lógica: activo = false.")
    @ApiResponse(responseCode = "204", description = "Usuario desactivado")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "404", description = "No existe el usuario")
    @ApiResponse(responseCode = "409", description = "El admin intenta desactivarse a sí mismo")
    public void desactivar(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        usuarioService.desactivar(id, authService.usuarioActual(jwt));
    }
}
