package com.nutrivida.nutrivida_backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutValidoValidator implements ConstraintValidator<RutValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        // Si viene vacío, de eso se encarga @NotBlank
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return Rut.esValido(valor);
    }
}
