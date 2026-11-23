package com.nutrivida.nutrivida_backend.dto;

import com.nutrivida.nutrivida_backend.model.Servicio;

// Servicio del catálogo con los mismos campos que usaba el frontend (servicios.json)
public record ServicioResponse(
        Integer id,
        String codigo,
        String tipo,
        String categoria,
        Integer categoriaId,
        String nombre,
        String duracion,
        String modalidad,
        Integer precio,
        String descripcion) {

    public static ServicioResponse de(Servicio s) {
        return new ServicioResponse(s.getId(), s.getCodigo(), s.getTipo(), s.getCategoria().getNombre(),
                s.getCategoria().getId(), s.getNombre(), s.getDuracion(), s.getModalidad(), s.getPrecio(),
                s.getDescripcion());
    }
}
