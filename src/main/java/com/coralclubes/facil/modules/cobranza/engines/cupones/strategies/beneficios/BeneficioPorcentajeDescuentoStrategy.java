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
 * Estrategia que aplica un descuento basado en un porcentaje.
 * dentro del contexto debe estar definido el monto original de la transacción para calcular el descuento real.
 * La configuración del beneficio puede ser un JSON con la clave "descuento" o un valor numérico directo.
 * Ejemplos de configuración válida:
 * - {"descuento": 30}
 * - 30
 * - "30"
 * Si el porcentaje configurado es mayor a 100, se aplicará un descuento del 100%.
 * Si el porcentaje configurado es nulo, vacío o menor o igual a cero, no se aplicará ningún descuento.
 * se retorna el monto del descuento real aplicado, que será el resultado de multiplicar el monto original por el porcentaje configurado.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeneficioPorcentajeDescuentoStrategy implements CuponBeneficioStrategy {

    public static final String CLAVE = "PORCENTAJE_DESCUENTO";

    private final ObjectMapper objectMapper;

    @Override
    public String getClaveBeneficio() {
        return CLAVE;
    }

    @Override
    public ResultadoAplicacionBeneficio aplicar(CuponBeneficioResponse beneficio, CuponEvaluacionContexto contexto) {
        BigDecimal montoOriginal = contexto.montoOriginal() != null ? contexto.montoOriginal() : BigDecimal.ZERO;
        if (montoOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            return ResultadoAplicacionBeneficio.soloDescuento(BigDecimal.ZERO);
        }

        String configStr = beneficio.configuracionBeneficio();
        if (configStr == null || configStr.isBlank()) {
            return ResultadoAplicacionBeneficio.soloDescuento(BigDecimal.ZERO);
        }

        try {
            BigDecimal porcentajeValor = null;

            // Soporta JSON {"descuento": 30} o valor numérico directo "30"
            if (configStr.trim().startsWith("{")) {
                Map<String, Object> map = objectMapper.readValue(configStr, new TypeReference<>() {
                });
                Object descObj = map.get("porcentaje_descuento");
                if (descObj == null) {
                    descObj = map.get("descuento");
                }
                if (descObj instanceof Number n) {
                    porcentajeValor = BigDecimal.valueOf(n.doubleValue());
                } else if (descObj instanceof String s) {
                    porcentajeValor = new BigDecimal(s.trim());
                }
            } else {
                porcentajeValor = new BigDecimal(configStr.trim());
            }

            if (porcentajeValor == null || porcentajeValor.compareTo(BigDecimal.ZERO) <= 0) {
                return ResultadoAplicacionBeneficio.soloDescuento(BigDecimal.ZERO);
            }

            // Si viene en base 100 (ej: 30 para 30%)
            if (porcentajeValor.compareTo(BigDecimal.ONE) > 0) {
                porcentajeValor = porcentajeValor.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            }

            BigDecimal descuento = montoOriginal.multiply(porcentajeValor).setScale(2, RoundingMode.HALF_UP);
            return ResultadoAplicacionBeneficio.soloDescuento(descuento);
        } catch (Exception e) {
            log.warn("Error al evaluar beneficio porcentaje_descuento: '{}'", configStr, e);
            return ResultadoAplicacionBeneficio.soloDescuento(BigDecimal.ZERO);
        }
    }
}
