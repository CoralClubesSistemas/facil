package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.condiciones;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponCondicionStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CondicionDesarrolloDiasStrategy implements CuponCondicionStrategy {

    public static final String CLAVE = "DESARROLLO_DIAS";

    private final ObjectMapper objectMapper;

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

        if (contexto.idDesarrollo() == null) {
            return ResultadoValidacionCondicion.invalida("No se proporcionó el desarrollo en el contexto para validar los días permitidos por desarrollo.");
        }

        List<LocalDate> fechasAEvaluar = extraerFechas(contexto);
        if (fechasAEvaluar.isEmpty()) {
            return ResultadoValidacionCondicion.invalida("No se proporcionó fecha de consumo ni periodo para validar los días permitidos por desarrollo.");
        }

        try {
            // Ejemplo de JSON: [{"desarrollo": 3, "dias": [1,2,3,4]}, {"desarrollo": 4, "dias": [1,2,3,4]}]
            List<Map<String, Object>> listaConfig = objectMapper.readValue(valor.trim(), new TypeReference<>() {});
            int desarrolloActual = contexto.idDesarrollo();

            Map<String, Object> configParaDesarrollo = listaConfig.stream()
                    .filter(c -> {
                        Object desVal = c.get("desarrollo");
                        return desVal instanceof Number && ((Number) desVal).intValue() == desarrolloActual;
                    })
                    .findFirst()
                    .orElse(null);

            if (configParaDesarrollo == null) {
                return ResultadoValidacionCondicion.invalida(
                        "El cupón no está configurado para ser utilizado en el desarrollo actual (" + desarrolloActual + ")."
                );
            }

            Object diasObj = configParaDesarrollo.get("dias");
            if (diasObj instanceof List<?> diasList) {
                for (LocalDate fecha : fechasAEvaluar) {
                    DayOfWeek dayOfWeek = fecha.getDayOfWeek();
                    int diaNumero = dayOfWeek.getValue(); // 1 = Lunes, 7 = Domingo

                    boolean diaPermitido = diasList.stream()
                            .map(String::valueOf)
                            .map(String::trim)
                            .anyMatch(d -> d.equals(String.valueOf(diaNumero))
                                    || d.equalsIgnoreCase(dayOfWeek.name())
                                    || d.equalsIgnoreCase(traducirDiaEspanol(dayOfWeek)));

                    if (!diaPermitido) {
                        return ResultadoValidacionCondicion.invalida(
                                String.format("El cupón no es válido para el día %s (%s) en el desarrollo seleccionado.",
                                        traducirDiaEspanol(dayOfWeek), fecha)
                        );
                    }
                }
            }

            return ResultadoValidacionCondicion.valida();
        } catch (Exception e) {
            log.warn("Error al parsear o evaluar JSON de desarrollo_dias: '{}'", valor, e);
            return ResultadoValidacionCondicion.valida();
        }
    }

    private List<LocalDate> extraerFechas(CuponEvaluacionContexto contexto) {
        List<LocalDate> lista = new ArrayList<>();

        LocalDate inicio = contexto.getAtributo("fechaEntrada", LocalDate.class)
                .or(() -> contexto.getAtributo("fechaInicio", LocalDate.class))
                .or(() -> contexto.getAtributo("fechaEntrada", LocalDateTime.class).map(LocalDateTime::toLocalDate))
                .or(() -> contexto.getAtributo("fechaInicio", LocalDateTime.class).map(LocalDateTime::toLocalDate))
                .orElse(null);

        LocalDate fin = contexto.getAtributo("fechaSalida", LocalDate.class)
                .or(() -> contexto.getAtributo("fechaFin", LocalDate.class))
                .or(() -> contexto.getAtributo("fechaSalida", LocalDateTime.class).map(LocalDateTime::toLocalDate))
                .or(() -> contexto.getAtributo("fechaFin", LocalDateTime.class).map(LocalDateTime::toLocalDate))
                .orElse(null);

        if (inicio != null && fin != null) {
            LocalDate actual = inicio;
            if (fin.isAfter(inicio)) {
                while (actual.isBefore(fin)) {
                    lista.add(actual);
                    actual = actual.plusDays(1);
                }
            } else {
                lista.add(inicio);
            }
            return lista;
        }

        if (inicio != null) {
            lista.add(inicio);
        } else if (contexto.fechaConsumo() != null) {
            lista.add(contexto.fechaConsumo());
        }

        return lista;
    }

    private String traducirDiaEspanol(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }
}
