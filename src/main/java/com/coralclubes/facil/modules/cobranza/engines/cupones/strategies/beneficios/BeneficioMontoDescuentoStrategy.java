package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.beneficios;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponBeneficioResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoAplicacionBeneficio;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponBeneficioStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Estrategia que aplica un descuento basado en un monto fijo.
 * dentro del contexto debe estar definido el monto original de la transacción para calcular el descuento real.
 * La configuración del beneficio puede ser un JSON con la clave "monto" o un valor numérico directo.
 * Ejemplos de configuración válida:
 * - {"monto": 50.00}
 * - 50.00
 * - "50.00"
 * Si el monto configurado es mayor al monto original, se aplicará un descuento igual al monto original.
 * Si el monto configurado es nulo, vacío o menor o igual a cero, no se aplicará ningún descuento.
 * se retorna el monto del descuento real aplicado, que será el menor entre el monto configurado y el monto original.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficioMontoDescuentoStrategy implements CuponBeneficioStrategy {

    public static final String CLAVE = "MONTO_DESCUENTO";

    private final ObjectMapper objectMapper;

    @Override
    public String getClaveBeneficio() {
        return CLAVE;
    }

    @Override
    public ResultadoAplicacionBeneficio aplicar(CuponBeneficioResponse beneficio, CuponEvaluacionContexto contexto) {
        String configStr = beneficio.configuracionBeneficio();
        if (configStr == null || configStr.isBlank()) {
            return ResultadoAplicacionBeneficio.soloDescuento(BigDecimal.ZERO);
        }

        try {
            BigDecimal monto = null;

            // Soporta JSON {"monto": 50.00} o valor numérico directo "50.00"
            if (configStr.trim().startsWith("{")) {
                Map<String, Object> map = objectMapper.readValue(configStr, new TypeReference<>() {
                });
                Object montoObj = map.get("monto");
                if (montoObj instanceof Number n) {
                    monto = BigDecimal.valueOf(n.doubleValue());
                } else if (montoObj instanceof String s) {
                    monto = new BigDecimal(s.trim());
                }
            } else {
                monto = new BigDecimal(configStr.trim());
            }

            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                return ResultadoAplicacionBeneficio.soloDescuento(BigDecimal.ZERO);
            }

            BigDecimal montoOriginal = contexto.montoOriginal() != null ? contexto.montoOriginal() : BigDecimal.ZERO;
            BigDecimal descuentoReal = monto.min(montoOriginal).setScale(2, RoundingMode.HALF_UP);

            return ResultadoAplicacionBeneficio.soloDescuento(descuentoReal);
        } catch (Exception e) {
            log.warn("Error al evaluar beneficio monto_descuento: '{}'", configStr, e);
            return ResultadoAplicacionBeneficio.soloDescuento(BigDecimal.ZERO);
        }
    }
}
