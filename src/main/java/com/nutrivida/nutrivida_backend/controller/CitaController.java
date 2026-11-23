package com.nutrivida.nutrivida_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nutrivida.nutrivida_backend.config.OpenApiConfig;
import com.nutrivida.nutrivida_backend.dto.CambiarEstadoCitaRequest;
import com.nutrivida.nutrivida_backend.dto.CitaRequest;
import com.nutrivida.nutrivida_backend.dto.CitaResponse;
import com.nutrivida.nutrivida_backend.dto.ReprogramarCitaRequest;
import com.nutrivida.nutrivida_backend.service.AuthService;
import com.nutrivida.nutrivida_backend.service.CitaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Solicitudes de cita. Todas las rutas exigen JWT (401 sin token).
// El PACIENTE solo ve y modifica sus propias solicitudes (403 con las ajenas).
@RestController
@RequestMapping("/api/v1/citas")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@Tag(name = "Citas", description = "Solicitudes de hora: crear, listar, cancelar, reprogramar y cambiar estado")
@ApiResponse(responseCode = "401", description = "Token ausente, alterado o vencido")
public class CitaController {

    private final CitaService citaService;
    private final AuthService authService;

    public CitaController(CitaService citaService, AuthService authService) {
        this.citaService = citaService;
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicitar una cita", description = "Cualquier usuario con sesión. Queda en estado Pendiente.")
    @ApiResponse(responseCode = "201", description = "Solicitud creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, fecha pasada u hora fuera de la jornada")
    @ApiResponse(responseCode = "404", description = "No existe el servicio")
    @ApiResponse(responseCode = "409", description = "El horario ya está tomado")
    public CitaResponse crear(@Valid @RequestBody CitaRequest datos, @AuthenticationPrincipal Jwt jwt) {
        return citaService.crear(datos, authService.usuarioActual(jwt));
    }

    @GetMapping
    @Operation(summary = "Listar solicitudes", description = "PACIENTE: solo las suyas. ADMIN y NUTRICIONISTA: todas.")
    @ApiResponse(responseCode = "200", description = "Lista de solicitudes")
    public List<CitaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return citaService.listar(authService.usuarioActual(jwt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver una solicitud", description = "El dueño, el ADMIN o un NUTRICIONISTA.")
    @ApiResponse(responseCode = "200", description = "Datos de la solicitud")
    @ApiResponse(responseCode = "403", description = "Es la solicitud de otra persona")
    @ApiResponse(responseCode = "404", description = "No existe la solicitud")
    public CitaResponse ver(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        return citaService.ver(id, authService.usuarioActual(jwt));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar una solicitud", description = "El dueño o el ADMIN. Libera el horario.")
    @ApiResponse(responseCode = "200", description = "Solicitud cancelada")
    @ApiResponse(responseCode = "403", description = "Es la solicitud de otra persona")
    @ApiResponse(responseCode = "404", description = "No existe la solicitud")
    @ApiResponse(responseCode = "409", description = "Ya estaba cancelada o rechazada")
    public CitaResponse cancelar(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        return citaService.cancelar(id, authService.usuarioActual(jwt));
    }

    @PatchMapping("/{id}/reprogramar")
    @Operation(summary = "Reprogramar una solicitud", description = "El dueño o el ADMIN. Queda en estado Reprogramada.")
    @ApiResponse(responseCode = "200", description = "Solicitud reprogramada")
    @ApiResponse(responseCode = "400", description = "Fecha pasada u hora fuera de la jornada")
    @ApiResponse(responseCode = "403", description = "Es la solicitud de otra persona")
    @ApiResponse(responseCode = "409", description = "El horario nuevo está tomado o la cita ya no está activa")
    public CitaResponse reprogramar(@PathVariable Integer id, @Valid @RequestBody ReprogramarCitaRequest datos,
            @AuthenticationPrincipal Jwt jwt) {
        return citaService.reprogramar(id, datos, authService.usuarioActual(jwt));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado", description = "Solo ADMIN: Confirmada, Rechazada, Pendiente, Reprogramada o Cancelada.")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Estado inválido")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMIN")
    @ApiResponse(responseCode = "404", description = "No existe la solicitud")
    @ApiResponse(responseCode = "409", description = "Al reactivarla, el horario ya está tomado")
    public CitaResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoCitaRequest datos) {
        return citaService.cambiarEstado(id, datos);
    }
}
