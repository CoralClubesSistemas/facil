package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.condiciones;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponCondicionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Estrategia que evalúa si la(s) temporada(s) del consumo están contenidas en el valor de la condición.
 *
 * <p>El contexto puede proporcionar:
 * <ul>
 *   <li>Un único valor: {@code idTemporada} (Integer/String) o {@code temporada} (String).</li>
 *   <li>Una lista o colección: {@code temporadas}, {@code temporadasIds}, {@code idsTemporadas}
 *       o {@code listaTemporadas} (de tipo {@code Collection<?>} o {@code String} separado por comas).</li>
 * </ul>
 *
 * <p><b>Regla:</b> Todas las temporadas recibidas en el contexto deben estar contenidas dentro del
 * catálogo/lista configurada en {@code valorCondicion} del cupón (por ejemplo {@code "[861,862,863]"}).
 * Si al menos una temporada del consumo no está permitida, el cupón es rechazado.
 */
@Slf4j
@Component
public class CondicionTemporadaStrategy implements CuponCondicionStrategy {

    public static final String CLAVE = "TEMPORADA";

    @Override
    public String getClaveCondicion() {
        return CLAVE;
    }

    @Override
    public ResultadoValidacionCondicion evaluar(CuponCondicionResponse condicion, CuponEvaluacionContexto contexto) {
        String valor = condicion.valorCondicion();
        if (valor == null || valor.isBlank()) {
            return ResultadoValidacionCondicion.valida();
        }

        // Limpieza de formato de la condición: soporta "[861,862,863]" o "861,862,863"
        String valorLimpio = valor.trim().replace("[", "").replace("]", "").replace("\"", "");
        if (valorLimpio.isBlank()) {
            return ResultadoValidacionCondicion.valida();
        }

        List<String> temporadasPermitidas = Arrays.stream(valorLimpio.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .toList();

        // Extraer todas las temporadas presentes en el contexto (pueden ser una o una lista)
        List<String> temporadasContexto = extraerTemporadasDelContexto(contexto);

        if (temporadasContexto.isEmpty()) {
            return ResultadoValidacionCondicion.invalida("No se proporcionó información de temporada en el contexto de consumo.");
        }

        // Evaluar que TODAS las temporadas del contexto estén contenidas en las temporadas permitidas
        for (String temp : temporadasContexto) {
            if (!temporadasPermitidas.contains(temp)) {
                log.info("Temporada '{}' del consumo no está dentro de las temporadas permitidas por el cupón: {}",
                        temp, temporadasPermitidas);
                return ResultadoValidacionCondicion.invalida("El cupón no aplica para la temporada '" + temp + "' comprendida en el consumo.");
            }
        }

        return ResultadoValidacionCondicion.valida();
    }

    /**
     * Extrae de forma flexible todas las temporadas enviadas en el contexto,
     * ya sea como valores únicos o colecciones/listas de IDs o nombres.
     */
    private List<String> extraerTemporadasDelContexto(CuponEvaluacionContexto contexto) {
        List<String> resultado = new ArrayList<>();

        if (contexto.atributos() == null || contexto.atributos().isEmpty()) {
            return resultado;
        }

        // Claves posibles para colecciones o listas
        String[] clavesListas = {"temporadas", "temporadasIds", "idsTemporadas", "listaTemporadas"};
        for (String clave : clavesListas) {
            Object obj = contexto.atributos().get(clave);
            if (obj instanceof Collection<?> col) {
                for (Object item : col) {
                    if (item != null) {
                        String s = item.toString().trim().toUpperCase();
                        if (!s.isEmpty() && !resultado.contains(s)) {
                            resultado.add(s);
                        }
                    }
                }
            } else if (obj instanceof String s && s.contains(",")) {
                String limpio = s.replace("[", "").replace("]", "").replace("\"", "");
                Arrays.stream(limpio.split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .filter(item -> !item.isEmpty() && !resultado.contains(item))
                        .forEach(resultado::add);
            } else if (obj != null) {
                String s = obj.toString().trim().toUpperCase();
                if (!s.isEmpty() && !resultado.contains(s)) {
                    resultado.add(s);
                }
            }
        }

        // Claves posibles para valor individual
        String[] clavesIndividuales = {"idTemporada", "temporada", "temporadaId", "tipoTemporadaId"};
        for (String clave : clavesIndividuales) {
            Object obj = contexto.atributos().get(clave);
            if (obj != null) {
                String s = obj.toString().trim().toUpperCase();
                if (!s.isEmpty() && !resultado.contains(s)) {
                    resultado.add(s);
                }
            }
        }

        return resultado;
    }
}
