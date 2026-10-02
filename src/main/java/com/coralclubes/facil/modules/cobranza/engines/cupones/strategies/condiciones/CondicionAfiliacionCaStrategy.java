package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.condiciones;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponCondicionStrategy;
import org.springframework.stereotype.Component;

/**
 * Requiere que el contexto contenga en los atributos el campo 'esAfiliadoCa' o 'afiliadoCa' de tipo Boolean.
 * Si el valor de la condición es "true", el cupón solo será válido si el usuario es afiliado a CA (esAfiliadoCa = true).
 * Si el valor de la condición es "false", el cupón solo será válido si el usuario NO es afiliado a CA (esAfiliadoCa = false).
 * Si el valor de la condición es nulo o vacío, la condición se considera válida sin importar el estado de afiliación.
 */
@Component
public class CondicionAfiliacionCaStrategy implements CuponCondicionStrategy {

    public static final String CLAVE = "AFILIACION_CA";

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

        boolean requiereAfiliacionCa = Boolean.parseBoolean(valor.trim());

        // Se busca el atributo esAfiliadoCa o afiliadoCa en el contexto
        var esAfiliadoOpt = contexto.getAtributo("esAfiliadoCa", Boolean.class)
                .or(() -> contexto.getAtributo("afiliadoCa", Boolean.class));

        if (esAfiliadoOpt.isEmpty()) {
            return ResultadoValidacionCondicion.invalida("No se proporcionó información de afiliación CA en el contexto.");
        }

        boolean esAfiliado = esAfiliadoOpt.get();

        if (requiereAfiliacionCa && !esAfiliado) {
            return ResultadoValidacionCondicion.invalida("El cupón es exclusivo para miembros afiliados a CA.");
        }

        if (!requiereAfiliacionCa && esAfiliado) {
            return ResultadoValidacionCondicion.invalida("El cupón no aplica para miembros afiliados a CA.");
        }

        return ResultadoValidacionCondicion.valida();
    }
}
