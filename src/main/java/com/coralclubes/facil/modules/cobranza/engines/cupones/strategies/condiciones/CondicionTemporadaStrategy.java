package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.condiciones;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponCondicionStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CondicionTemporadaStrategy implements CuponCondicionStrategy {

    public static final String CLAVE = "TEMPORADA";

    @Override
    public String getClaveCondicion() {
        return CLAVE;
    }

    @Override
    public ResultadoValidacionCondicion evaluar(CuponCondicionResponse condicion, CuponEvaluacionContexto contexto) {
        String valor = condicion.valorCondicion();
        if (valor == null || valor.isBlank()) {
            return ResultadoValidacionCondicion.valida();
        }

        // Se busca el atributo 'idTemporada' o 'temporada' provisto en el contexto
        var idTemporadaOpt = contexto.getAtributo("idTemporada", Integer.class);
        var temporadaOpt = contexto.getAtributo("temporada", String.class);

        if (idTemporadaOpt.isEmpty() && temporadaOpt.isEmpty()) {
            return ResultadoValidacionCondicion.invalida("No se proporcionó información de temporada en el contexto de consumo.");
        }

        // Limpieza de formato: soporta "[861,862,863]" o "861,862,863"
        String valorLimpio = valor.trim().replace("[", "").replace("]", "").replace("\"", "");
        if (valorLimpio.isBlank()) {
            return ResultadoValidacionCondicion.valida();
        }

        List<String> temporadasPermitidas = Arrays.stream(valorLimpio.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .toList();

        boolean coincide = idTemporadaOpt.map(id -> temporadasPermitidas.contains(String.valueOf(id))).orElse(false)
                || temporadaOpt.map(temp -> temporadasPermitidas.contains(temp.toUpperCase())).orElse(false);

        if (!coincide) {
            return ResultadoValidacionCondicion.invalida("El cupón no aplica para la temporada actual seleccionada.");
        }

        return ResultadoValidacionCondicion.valida();
    }
}
