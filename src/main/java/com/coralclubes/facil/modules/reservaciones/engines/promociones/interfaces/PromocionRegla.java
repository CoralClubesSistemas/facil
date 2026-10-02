package com.coralclubes.facil.modules.reservaciones.engines.promociones.interfaces;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Regla;
import com.coralclubes.facil.modules.reservaciones.engines.promociones.dto.ReservacionContexto;

public interface PromocionRegla {
    /**
     * @return La clave de la regla en BD (ej: "HOTEL", "TEMPORADA", "HABITACION")
     */
    String getTipoRegla();

    /**
     * Evalúa si el carrito actual cumple con esta condición específica.
     */
    boolean evaluate(Regla ruleData, ReservacionContexto context);
}
