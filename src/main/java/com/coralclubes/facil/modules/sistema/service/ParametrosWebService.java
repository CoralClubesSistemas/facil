package com.coralclubes.facil.modules.sistema.service;

import com.coralclubes.facil.modules.sistema.dto.response.ParametrosWeb;
import com.coralclubes.facil.modules.sistema.repository.ParametrosWebRepository;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para la gestión de parámetros web del sistema.
 * Consume el repositorio para obtener configuraciones y datos generales del aplicativo.
 */
@Service
@RequiredArgsConstructor
public class ParametrosWebService {

    private final ParametrosWebRepository parametrosWebRepository;

    public ApiResponse<String> obtenerParametroWebVersion() {
        String clave = "VERSION";

        return parametrosWebRepository.spFacilObtenerParametroWeb(clave)
                .map(parametro -> ApiResponse.success(
                        "Parámetro web obtenido exitosamente",
                        parametro
                ))
                .orElseGet(() -> ApiResponse.error(
                        GeneralResponseCode.NOT_FOUND,
                        "El parámetro web '" + clave + "' no fue encontrado en el sistema"
                ));
    }

    public ApiResponse<List<ParametrosWeb>> obtenerParametrosWeb() {
        List<ParametrosWeb> parametros = parametrosWebRepository.spFacilObtenerParametrosWeb();

        if (parametros.isEmpty()) {
            return ApiResponse.error(
                    GeneralResponseCode.NOT_FOUND,
                    "No se encontraron parámetros web en el sistema"
            );
        }

        return ApiResponse.success(
                "Parámetros web obtenidos exitosamente",
                parametros
        );
    }
}


