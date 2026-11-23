package com.nutrivida.nutrivida_backend.dto;

import com.nutrivida.nutrivida_backend.model.Categoria;

// Categoría con la cantidad de servicios activos que tiene
public record CategoriaResponse(Integer id, String nombre, long totalServicios) {

    public static CategoriaResponse de(Categoria c, long totalServicios) {
        return new CategoriaResponse(c.getId(), c.getNombre(), totalServicios);
    }
}
