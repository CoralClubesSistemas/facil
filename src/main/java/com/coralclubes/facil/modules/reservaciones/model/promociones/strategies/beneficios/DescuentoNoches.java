package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.beneficios;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Beneficio;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionBeneficio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DescuentoNoches implements PromocionBeneficio {

    @Override
    public String getTipoBeneficio() { return "NOCHE"; }

    @Override
    public BigDecimal calculateDiscount(Beneficio benefitData, ReservacionContexto context) {
        int nochesARegalar = benefitData.valor().intValue();
        BigDecimal descuentoAcumulado = BigDecimal.ZERO;

        // Obtenemos el desglose diario que ya viene cargado en memoria
        List<BigDecimal> costosDiarios = context.getUnidadElegidaParaDescuento().getCostoPorNoche();

        // Nos aseguramos de no iterar más noches de las que realmente tiene la reserva
        int limite = Math.min(nochesARegalar, costosDiarios.size());

        for (int i = 0; i < limite; i++) {
            descuentoAcumulado = descuentoAcumulado.add(costosDiarios.get(i));
        }

        return switch (benefitData.objetivoClave().toUpperCase()) {
            case "TOTAL", "HABITACION" -> {
                BigDecimal costoUnidad = context.getUnidadElegidaParaDescuento().getCostoEstancia();
                yield descuentoAcumulado.compareTo(costoUnidad) > 0 ? costoUnidad : descuentoAcumulado;
            }
            case "PROX_RESERV" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }
}