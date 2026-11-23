package com.nutrivida.nutrivida_backend.dto;

import com.nutrivida.nutrivida_backend.model.Nutricionista;

public record NutricionistaResponse(
        Integer id, String codigo, String nombre, String especialidad, String dias, String horario) {

    public static NutricionistaResponse de(Nutricionista n) {
        return new NutricionistaResponse(n.getId(), n.getCodigo(), n.getNombre(), n.getEspecialidad(),
                n.getDias(), n.getHorario());
    }
}
