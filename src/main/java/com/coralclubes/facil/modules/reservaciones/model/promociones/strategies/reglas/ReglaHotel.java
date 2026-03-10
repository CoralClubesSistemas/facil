package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.reglas;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Regla;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionRegla;
import com.coralclubes.facil.modules.reservaciones.model.promociones.utils.EvaluadorReglasUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReglaHotel implements PromocionRegla {
    @Override
    public String getTipoRegla() { return "HOTEL"; }

    @Override
    public boolean evaluate(Regla ruleData, ReservacionContexto context) {
        List<Integer> hotelesPermitidos = ruleData.detalles().stream()
                .map(d -> d.valorCatalogoId())
                .toList();

        // Validamos contra el destino general del carrito
        return EvaluadorReglasUtil.compararListas(context.getIdDesarrollo(), ruleData.comparadorClave(), hotelesPermitidos);
    }
}