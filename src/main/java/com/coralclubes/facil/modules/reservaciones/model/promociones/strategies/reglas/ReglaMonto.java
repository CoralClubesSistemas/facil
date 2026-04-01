package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.reglas;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Regla;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionRegla;
import com.coralclubes.facil.modules.reservaciones.model.promociones.utils.EvaluadorReglasUtil;
import org.springframework.stereotype.Component;

@Component
public class ReglaMonto implements PromocionRegla {
    @Override
    public String getTipoRegla() { return "MONTO"; }

    @Override
    public boolean evaluate(Regla ruleData, ReservacionContexto context) {
        if (ruleData.detalles().isEmpty()) return false;

        var detalle = ruleData.detalles().get(0);

        // Evalúa si el TOTAL del carrito alcanza el mínimo requerido por la promoción
        return EvaluadorReglasUtil.compararNumericos(
                context.getMontoTotalCarrito(),
                ruleData.comparadorClave(),
                detalle.valorNumerico(),
                detalle.valorSecundario()
        );
    }
}