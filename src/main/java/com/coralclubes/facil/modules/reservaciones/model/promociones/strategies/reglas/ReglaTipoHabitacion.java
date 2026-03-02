package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.reglas;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Regla;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionRegla;
import com.coralclubes.facil.modules.reservaciones.model.promociones.utils.EvaluadorReglasUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReglaTipoHabitacion implements PromocionRegla {
    @Override
    public String getTipoRegla() { return "HABITACION"; }

    @Override
    public boolean evaluate(Regla ruleData, ReservacionContexto context) {
        List<Integer> habitacionesPermitidas = ruleData.detalles().stream()
                .map(d -> d.valorCatalogoId())
                .toList();

        return EvaluadorReglasUtil.compararListas(context.getIdTipoHabitacion(), ruleData.comparadorClave(), habitacionesPermitidas);
    }
}