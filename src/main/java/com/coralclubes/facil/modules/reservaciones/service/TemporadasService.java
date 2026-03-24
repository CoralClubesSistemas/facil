package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.request.TemporadaMasivaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.TemporadaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.TemporadaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TemporadaFechaResponse;
import com.coralclubes.facil.modules.reservaciones.repository.TemporadasRepository;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemporadasService {

    private final TemporadasRepository repository;
    private final UserContext userContext;

    public ApiResponse<List<TemporadaDto>> obtenerTemporadas(Integer anio) {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        List<TemporadaDto> temporadas = repository.spResvObtenerTemporadasReservaciones(idDesarrollo, anio);
        return ApiResponse.success(temporadas);
    }

    public ApiResponse<Integer> guardarTemporada(TemporadaRequest request) {
        // Validamos en código también por si acaso, aunque el SP ya lo hace
        if (request.fechaInicio().isAfter(request.fechaFinal())) {
            return ApiResponse.error(GeneralResponseCode.SERVICE_UNAVAILABLE, "La fecha de inicio no puede ser mayor a la fecha final.");
        }

        String usuario = userContext.getUsername();

        Integer idGenerado = repository.spResvGuardarTemporadaReservacion(request, usuario)
                .orElseThrow(() -> new ServiceUnavailableException("No se pudo guardar la temporada."));

        return ApiResponse.success("Temporada guardada correctamente", idGenerado);
    }

    public ApiResponse<Boolean> eliminarTemporada(Integer idTemporadaFecha) {
        String usuario = userContext.getUsername();

        repository.spResvEliminarTemporadaReservacion(idTemporadaFecha, usuario);

        return ApiResponse.success("Temporada eliminada correctamente", true);
    }

    public ApiResponse<List<TemporadaFechaResponse>> obtenerTemporadasPorFecha(LocalDate fecha) {
        Integer idDesarrollo = userContext.getIdDesarrollo();
        if (idDesarrollo == null) idDesarrollo = 0;

        List<TemporadaFechaResponse> actuales = repository.spResvObtenerTemporadasFecha(idDesarrollo, fecha);
        return ApiResponse.success(actuales);
    }

    public ApiResponse<Integer> guardarTemporadasMasivas(List<TemporadaMasivaRequest> request) {
        if (request == null || request.isEmpty()) {
            return ApiResponse.error(GeneralResponseCode.BAD_REQUEST, "La lista de temporadas está vacía.");
        }

        String usuario = userContext.getUsername();

        Integer insertados = repository.spResvGuardarTemporadasMasivas(request, usuario)
                .orElseThrow(() -> new ServiceUnavailableException("Error al procesar la carga masiva."));

        return ApiResponse.success("Se insertaron " + insertados + " temporadas correctamente.", insertados);
    }
}