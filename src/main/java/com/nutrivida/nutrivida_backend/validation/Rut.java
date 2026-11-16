package com.nutrivida.nutrivida_backend.validation;

// Utilidades para el RUT chileno (misma regla que validarRutChileno del frontend).
public final class Rut {

    private Rut() {
    }

    // "12.345.678-5" o "123456785" -> "12345678-5". Así un mismo RUT no se guarda dos veces con formatos distintos.
    public static String normalizar(String rut) {
        if (rut == null) {
            return null;
        }
        String limpio = rut.replace(".", "").replace("-", "").replace(" ", "").trim().toUpperCase();
        if (limpio.length() < 2) {
            return limpio;
        }
        return limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
    }

    // Comprueba el dígito verificador con el algoritmo módulo 11
    public static boolean esValido(String rut) {
        String normalizado = normalizar(rut);
        if (normalizado == null || !normalizado.matches("\\d{7,8}-[\\dK]")) {
            return false;
        }
        String cuerpo = normalizado.substring(0, normalizado.length() - 2);
        char dvIngresado = normalizado.charAt(normalizado.length() - 1);

        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        char dvEsperado = resto == 11 ? '0' : resto == 10 ? 'K' : Character.forDigit(resto, 10);
        return dvIngresado == dvEsperado;
    }
}
