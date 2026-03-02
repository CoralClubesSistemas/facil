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
            case "TOTAL" -> benefitData.valor();
            case "PROX_RESERV" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };

        // Regla de negocio: El descuento no puede ser mayor al total a pagar
        return descuento.compareTo(context.getMontoActual()) > 0 ? context.getMontoActual() : descuento;
    }
}