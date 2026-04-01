package com.coralclubes.facil.modules.reservaciones.model.promociones.utils;

import java.math.BigDecimal;
import java.util.List;

public class EvaluadorReglasUtil {

    public static boolean compararListas(Integer valorContexto, String operador, List<Integer> valoresPermitidos) {
        if (valorContexto == null || valoresPermitidos == null || valoresPermitidos.isEmpty()) {
            return false;
        }

        return switch (operador.toUpperCase()) {
            case "IN", "=" -> valoresPermitidos.contains(valorContexto);
            case "NOT_IN", "<>" -> !valoresPermitidos.contains(valorContexto);
            default -> false;
        };
    }

    public static boolean compararNumericos(BigDecimal valorContexto, String operador, BigDecimal valorRegla, BigDecimal valorSecundario) {
        if (valorContexto == null || valorRegla == null) return false;

        int comparacion = valorContexto.compareTo(valorRegla);

        return switch (operador.toUpperCase()) {
            case "=" -> comparacion == 0;
            case ">" -> comparacion > 0;
            case ">=" -> comparacion >= 0;
            case "<" -> comparacion < 0;
            case "<=" -> comparacion <= 0;
            case "<>" -> comparacion != 0;
            case "BETWEEN" -> valorSecundario != null
                    && comparacion >= 0
                    && valorContexto.compareTo(valorSecundario) <= 0;
            default -> false;
        };
    }
}