package com.nutrivida.nutrivida_backend.dto;

import java.time.format.DateTimeFormatter;

import com.nutrivida.nutrivida_backend.model.Cita;

// Solicitud de cita con los mismos campos que usaba el frontend (CitasContext)
public record CitaResponse(
        Integer id,
        Integer pacienteId,
        String nombre,
        String rut,
        String email,
        String telefono,
        Integer servicioId,
        String servicio,
        Integer precio,
        String nutricionistaCodigo,
        String nutricionista,
        String fecha,          // "2026-12-02"
        String hora,           // "10:30"
        String motivo,
        String estado,
        String fechaCreacion) { // "23-11-2026"

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_CL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static CitaResponse de(Cita c) {
        return new CitaResponse(c.getId(), c.getPaciente().getId(), c.getNombre(), c.getRut(), c.getEmail(),
                c.getTelefono(), c.getServicio().getId(), c.getServicio().getNombre(), c.getServicio().getPrecio(),
                c.getNutricionista().getCodigo(), c.getNutricionista().getNombre(), c.getFecha().toString(),
                c.getHora().format(HORA), c.getMotivo(), c.getEstado(),
                c.getCreadoEn() == null ? null : c.getCreadoEn().format(FECHA_CL));
    }
}
