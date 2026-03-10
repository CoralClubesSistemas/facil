package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.beneficios;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Beneficio;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionBeneficio;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DescuentoDinero implements PromocionBeneficio {
    @Override
    public String getTipoBeneficio() { return "DESC_MONEY"; }

    @Override
    public BigDecimal calculateDiscount(Beneficio benefitData, ReservacionContexto context) {
        BigDecimal descuento = switch (benefitData.objetivoClave().toUpperCase()) {
            case "TOTAL", "HABITACION" -> benefitData.valor();
            case "PROX_RESERV" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };

        // El descuento en dinero duro no puede ser mayor al costo de la unidad específica
        BigDecimal costoUnidad = context.getUnidadElegidaParaDescuento().getCostoEstancia();
        return descuento.compareTo(costoUnidad) > 0 ? costoUnidad : descuento;
    }
}