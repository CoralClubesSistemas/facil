package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.condiciones;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponCondicionStrategy;
import org.springframework.stereotype.Component;

/**
 * Estrategia que evalua si el número de personas/huéspedes en el consumo no excede el valor máximo permitido por la condición.
 * para ello el campo dentro de los atributos debe estar nombrado como 'personas', 'numeroPersonas', 'huespedes' o 'adultos' y debe ser de tipo Integer.
 * y el campo en el valor de la condicion debe ser un número entero que representa el máximo permitido.
 */
@Component
public class CondicionMaxPersonasStrategy implements CuponCondicionStrategy {

    public static final String CLAVE = "MAX_PERSONAS";

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

        int maxPermitido;
        try {
            maxPermitido = Integer.parseInt(valor.trim().replace("\"", ""));
        } catch (NumberFormatException e) {
            return ResultadoValidacionCondicion.valida();
        }

        // Se busca el número de personas/huéspedes en el contexto
        var personasOpt = contexto.getAtributo("personas", Integer.class)
                .or(() -> contexto.getAtributo("numeroPersonas", Integer.class))
                .or(() -> contexto.getAtributo("huespedes", Integer.class))
                .or(() -> contexto.getAtributo("adultos", Integer.class));

        if (personasOpt.isEmpty()) {
            return ResultadoValidacionCondicion.valida();
        }

        int personas = personasOpt.get();
        if (personas > maxPermitido) {
            return ResultadoValidacionCondicion.invalida(
                    String.format("El cupón permite un máximo de %d persona(s), pero el consumo especifica %d.", maxPermitido, personas)
            );
        }

        return ResultadoValidacionCondicion.valida();
    }
}
