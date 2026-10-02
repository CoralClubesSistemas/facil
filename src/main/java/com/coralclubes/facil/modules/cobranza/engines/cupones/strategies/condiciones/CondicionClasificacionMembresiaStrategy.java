package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.condiciones;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponCondicionStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CondicionClasificacionMembresiaStrategy implements CuponCondicionStrategy {

    public static final String CLAVE = "CLASIFICACION_MEMBRESIA";

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

        // Se busca clasificacionMembresiaId o clasificacionMembresia en atributos del contexto
        var clasificacionIdOpt = contexto.getAtributo("clasificacionMembresiaId", Integer.class);
        var clasificacionNombreOpt = contexto.getAtributo("clasificacionMembresia", String.class);

        if (clasificacionIdOpt.isEmpty() && clasificacionNombreOpt.isEmpty()) {
            return ResultadoValidacionCondicion.invalida("No se proporcionó información de clasificación de membresía en el contexto.");
        }

        // Limpieza de formato: soporta "[275,276]" o "275,276"
        String valorLimpio = valor.trim().replace("[", "").replace("]", "").replace("\"", "");
        if (valorLimpio.isBlank()) {
            return ResultadoValidacionCondicion.valida();
        }

        List<String> permitidas = Arrays.stream(valorLimpio.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .toList();

        boolean coincide = clasificacionIdOpt.map(id -> permitidas.contains(String.valueOf(id))).orElse(false)
                || clasificacionNombreOpt.map(nom -> permitidas.contains(nom.toUpperCase())).orElse(false);

        if (!coincide) {
            return ResultadoValidacionCondicion.invalida("El cupón no es aplicable para la clasificación de su membresía.");
        }

        return ResultadoValidacionCondicion.valida();
    }
}
