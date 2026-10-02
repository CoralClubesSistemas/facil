package com.coralclubes.facil.modules.cobranza.engines.cupones.strategies.condiciones;

import com.coralclubes.facil.modules.cobranza.dto.response.CuponCondicionResponse;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.CuponEvaluacionContexto;
import com.coralclubes.facil.modules.cobranza.engines.cupones.dto.ResultadoValidacionCondicion;
import com.coralclubes.facil.modules.cobranza.engines.cupones.interfaces.CuponCondicionStrategy;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CondicionDiasConsumoStrategy implements CuponCondicionStrategy {

    public static final String CLAVE = "DIAS";

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

        // Limpieza de formato: soporta "[1,2,3,4,5]" o "1,2,3,4,5"
        String valorLimpio = valor.trim().replace("[", "").replace("]", "").replace("\"", "");
        if (valorLimpio.isBlank()) {
            return ResultadoValidacionCondicion.valida();
        }

        List<String> diasPermitidos = Arrays.stream(valorLimpio.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .toList();

        // 1. Obtener lista de fechas a evaluar (soporta periodo inicio-fin o fecha individual)
        List<LocalDate> fechasAEvaluar = extraerFechas(contexto);

        if (fechasAEvaluar.isEmpty()) {
            return ResultadoValidacionCondicion.invalida("No se proporcionó fecha de consumo ni periodo para validar los días permitidos.");
        }

        // 2. Evaluar que cada fecha del consumo o de la estancia cumpla con los días permitidos
        for (LocalDate fecha : fechasAEvaluar) {
            DayOfWeek diaSemana = fecha.getDayOfWeek();
            int diaNum = diaSemana.getValue(); // 1 = Lunes, 7 = Domingo

            boolean coincide = diasPermitidos.contains(String.valueOf(diaNum))
                    || diasPermitidos.contains(diaSemana.name())
                    || diasPermitidos.contains(traducirDiaEspanol(diaSemana));

            if (!coincide) {
                return ResultadoValidacionCondicion.invalida(
                        String.format("El cupón no es válido para el día %s (%s).", traducirDiaEspanol(diaSemana), fecha)
                );
            }
        }

        return ResultadoValidacionCondicion.valida();
    }

    private List<LocalDate> extraerFechas(CuponEvaluacionContexto contexto) {
        List<LocalDate> lista = new ArrayList<>();

        // Revisar si viene un rango/periodo en los atributos (ej. fechaEntrada / fechaSalida o fechaInicio / fechaFin)
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
            // En hotelería / estancias, la noche ocupada va desde 'inicio' hasta el día previo al checkout 'fin'
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

        // Si no hay rango de fechas, evaluar la fecha individual
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
