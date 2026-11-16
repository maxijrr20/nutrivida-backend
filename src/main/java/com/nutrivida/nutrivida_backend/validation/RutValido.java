package com.nutrivida.nutrivida_backend.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// Anotación propia de validación: @RutValido revisa el formato y el dígito verificador.
// Si falla, la API responde 400 con el mensaje en el campo "rut".
@Documented
@Constraint(validatedBy = RutValidoValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RutValido {

    String message() default "El RUT no es válido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
