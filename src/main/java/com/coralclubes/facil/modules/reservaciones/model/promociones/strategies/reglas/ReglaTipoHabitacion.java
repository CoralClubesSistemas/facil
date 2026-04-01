package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.reglas;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Regla;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionRegla;
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

        // Iteramos sobre las habitaciones del carrito
        for (ReservacionContexto.ItemContexto item : context.getItems()) {
            boolean coincide = false;

            if (ruleData.comparadorClave().equalsIgnoreCase("IN") || ruleData.comparadorClave().equals("=")) {
                coincide = habitacionesPermitidas.contains(item.getIdTipoHabitacion());
            } else if (ruleData.comparadorClave().equalsIgnoreCase("NOT_IN") || ruleData.comparadorClave().equals("<>")) {
                coincide = !habitacionesPermitidas.contains(item.getIdTipoHabitacion());
            }

            // Si encuentra una que coincide, la marca como el "Target"
            // del descuento y retorna TRUE inmediatamente.
            if (coincide) {
                context.setUnidadElegidaParaDescuento(item);
                return true;
            }
        }

        return false;
    }
}