package com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Beneficio;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import java.math.BigDecimal;

public interface PromocionBeneficio {
    /**
     * @return La clave del beneficio en BD (ej: "DESC_MONEY", "PCT_DESC")
     */
    String getTipoBeneficio();

    /**
     * Calcula exactamente cuánto dinero se le va a descontar al usuario.
     */
    BigDecimal calculateDiscount(Beneficio benefitData, ReservacionContexto context);
}