package com.coralclubes.facil.modules.reservaciones.model.promociones.engine;

import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion;
import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Beneficio;
import com.coralclubes.facil.modules.reservaciones.dto.response.Promocion.Regla;
import com.coralclubes.facil.modules.reservaciones.model.promociones.dto.ReservacionContexto;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionBeneficio;
import com.coralclubes.facil.modules.reservaciones.model.promociones.interfaces.PromocionRegla;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PromocionesEngine {

    private final Map<String, PromocionRegla> mapaReglas;
    private final Map<String, PromocionBeneficio> mapaBeneficios;

    // Spring Inyecta TODAS las clases que implementan las interfaces automáticamente en estas listas
    public PromocionesEngine(List<PromocionRegla> reglas, List<PromocionBeneficio> beneficios) {
        this.mapaReglas = reglas.stream()
                .collect(Collectors.toMap(PromocionRegla::getTipoRegla, Function.identity()));

        this.mapaBeneficios = beneficios.stream()
                .collect(Collectors.toMap(PromocionBeneficio::getTipoBeneficio, Function.identity()));
    }

    /**
     * Evalúa la promoción completa contra el contexto de la reservación actual.
     * @return El monto total a descontar de la reservación (0 si no cumple reglas).
     */
    public BigDecimal evaluarYAplicar(Promocion promocion, ReservacionContexto contexto) {

        // 1. FASE DE REGLAS (MATCHING)
        for (Regla regla : promocion.reglas()) {
            PromocionRegla estrategiaRegla = mapaReglas.get(regla.tipoReglaClave());

            if (estrategiaRegla == null) {
                throw new IllegalArgumentException("Motor no soporta la regla: " + regla.tipoReglaClave());
            }

            // Si UNA sola regla falla (Ej. No es el hotel correcto), la promo entera se descarta.
            if (!estrategiaRegla.evaluate(regla, contexto)) {
                return BigDecimal.ZERO;
            }
        }

        // 2. FASE DE BENEFICIOS (CALCULATING)
        BigDecimal totalDescuentoCalculado = BigDecimal.ZERO;

        for (Beneficio beneficio : promocion.beneficios()) {
            PromocionBeneficio estrategiaBeneficio = mapaBeneficios.get(beneficio.tipoBeneficioClave());

            if (estrategiaBeneficio != null) {
                BigDecimal descuentoParcial = estrategiaBeneficio.calculateDiscount(beneficio, contexto);
                totalDescuentoCalculado = totalDescuentoCalculado.add(descuentoParcial);
            }
        }

        return totalDescuentoCalculado;
    }
}