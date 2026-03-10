package com.coralclubes.facil.modules.reservaciones.model.promociones.strategies.beneficios;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Beneficio;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionBeneficio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DescuentoPuntos implements PromocionBeneficio {

    @Override
    public String getTipoBeneficio() { return "PUNTOS"; }

    @Override
    public BigDecimal calculateDiscount(Beneficio benefitData, ReservacionContexto context) {
        // Los puntos son una recompensa diferida, no un descuento en efectivo sobre la cuenta actual.
        // Al procesar el guardado final (Checkout), se leerá que esta promo otorgó X puntos
        // y se insertarán en el estado de cuenta del socio, pero aquí el descuento contable es 0.
        return BigDecimal.ZERO;
    }
}