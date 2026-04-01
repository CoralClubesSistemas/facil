package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.beneficios;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Beneficio;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionBeneficio;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DescuentoPorcentaje implements PromocionBeneficio {
    @Override
    public String getTipoBeneficio() { return "PCT_DESC"; }

    @Override
    public BigDecimal calculateDiscount(Beneficio benefitData, ReservacionContexto context) {
        BigDecimal porcentaje = benefitData.valor().divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);

        // OBTENEMOS EL COSTO DE LA UNIDAD ESPECÍFICA, NO DEL CARRITO TOTAL
        BigDecimal costoUnidad = context.getUnidadElegidaParaDescuento().getCostoEstancia();

        return switch (benefitData.objetivoClave().toUpperCase()) {
            case "TOTAL", "HABITACION" -> costoUnidad.multiply(porcentaje);
            case "PROX_RESERV" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }
}