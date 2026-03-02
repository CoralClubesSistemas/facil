package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.request.TarifaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.TarifaDto;
import com.coralclubes.facil.modules.reservaciones.repository.TarifasRepository;
import com.coralclubes.facil.shared.infrastructure.exceptions.custom.ServiceUnavailableException;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifasService {

    private final TarifasRepository repository;
    private final UserContext userContext;

    public ApiResponse<List<TarifaDto>> obtenerTarifas(Integer anio) {
        // Extraemos el idDesarrollo del usuario en sesión
        Integer idDesarrollo = userContext.getIdDesarrollo();

        List<TarifaDto> tarifas = repository.spResvObtenerTarifasReservaciones(idDesarrollo, anio);
        return ApiResponse.success(tarifas);
    }

    public ApiResponse<Integer> guardarTarifas(List<TarifaRequest> request) {
        if (request == null || request.isEmpty()) {
            return ApiResponse.error(GeneralResponseCode.BAD_REQUEST, "No hay tarifas para procesar.");
        }

        String usuario = userContext.getUsername();

        Integer registrosAfectados = repository.spResvGuardarTarifasReservaciones(request, usuario)
                .orElseThrow(() -> new ServiceUnavailableException("No se pudieron guardar las tarifas."));

        return ApiResponse.success("Se registraron " + registrosAfectados + " tarifas correctamente", registrosAfectados);
    }

    public ApiResponse<Integer> eliminarTarifas(List<Integer> idsTarifas) {
        if (idsTarifas == null || idsTarifas.isEmpty()) {
            return ApiResponse.error(GeneralResponseCode.BAD_REQUEST, "Debe seleccionar al menos una tarifa para eliminar.");
        }

        String usuario = userContext.getUsername();

        Integer eliminados = repository.spResvEliminarTarifasReservaciones(idsTarifas, usuario)
                .orElseThrow(() -> new ServiceUnavailableException("Error al dar de baja las tarifas."));

        return ApiResponse.success("Se eliminaron " + eliminados + " tarifas correctamente", eliminados);
    }
}