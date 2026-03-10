package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.beneficios;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Beneficio;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionBeneficio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DescuentoPrecioFijo implements PromocionBeneficio {

    @Override
    public String getTipoBeneficio() { return "PRE_FIJO"; }

    @Override
    public BigDecimal calculateDiscount(Beneficio benefitData, ReservacionContexto context) {
        BigDecimal precioFijoObjetivo = benefitData.valor();
        BigDecimal costoUnidad = context.getUnidadElegidaParaDescuento().getCostoEstancia();
        BigDecimal descuento = BigDecimal.ZERO;

        switch (benefitData.objetivoClave().toUpperCase()) {
            case "TOTAL", "HABITACION" -> {
                descuento = costoUnidad.subtract(precioFijoObjetivo);
            }
            case "PRIMERA_NOCHE" -> {
                List<BigDecimal> costosDiarios = context.getUnidadElegidaParaDescuento().getCostoPorNoche();

                // Obtenemos el costo de la primera noche (indice 0)
                BigDecimal costoPrimeraNoche = costosDiarios.isEmpty() ? BigDecimal.ZERO : costosDiarios.getFirst();

                descuento = costoPrimeraNoche.subtract(precioFijoObjetivo);
            }
        }

        if (descuento.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return descuento.compareTo(costoUnidad) > 0 ? costoUnidad : descuento;
    }
}