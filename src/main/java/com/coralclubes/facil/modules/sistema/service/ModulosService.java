package com.coralclubes.facil.modules.sistema.service;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.modules.sistema.dto.response.ModuloApiResponse;
import com.coralclubes.facil.modules.sistema.repository.ModulosRepository;
import com.coralclubes.facil.modules.sistema.mapper.ModuloResponseMapper;
import com.coralclubes.facil.shared.utils.TreeGenerator;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Servicio para gestionar los módulos del sistema.
 */
@Service
@RequiredArgsConstructor
public class ModulosService {

    private final ModulosRepository repo;

    /**
     * Obtiene todos los módulos del sistema y los formatea jerárquicamente.
     */
    public ApiResponse<List<ModuloApiResponse>> obtenerTodosLosModulos() {
        List<ModuloDtoResult> allModules = repo.spFacilObtenerModulosSistema();

        if (allModules == null || allModules.isEmpty()) {
            return ApiResponse.success("No hay módulos definidos", Collections.emptyList());
        }

        ModuloResponseMapper mapper = new ModuloResponseMapper();

        return ApiResponse.success(
                TreeGenerator.generateTree(
                        allModules,
                        mapper::map,
                        ModuloDtoResult::id,
                        ModuloDtoResult::idPadre
                )
        );
    }

    /**
     * Guarda o actualiza un módulo.
     */
    public ApiResponse<Integer> guardarModulo(ModuloDtoResult modulo) {
        Integer idGenerado = repo.spFacilGuardarModulo(modulo);

        if (idGenerado == null) {
            return ApiResponse.error(GeneralResponseCode.SERVICE_UNAVAILABLE, "Error al guardar el módulo. Intente nuevamente.");
        }

        return ApiResponse.success(
                "Módulo guardado/actualizado correctamente",
                idGenerado
        );
    }

    /**
     * Elimina un módulo por ID.
     */
    public ApiResponse<Integer> eliminarModulo(Integer moduloId) {
        Integer result = repo.spFacilEliminarModulo(moduloId);

        if (result == null) {
            return ApiResponse.error(GeneralResponseCode.SERVICE_UNAVAILABLE, "Error al eliminar el módulo. Intente nuevamente.");
        }

        return ApiResponse.success(
                "Módulo eliminado correctamente",
                result
        );
    }

    /**
     * Obtiene la información detallada de un módulo y su módulo padre a partir de sus claves.
     *
     * @param clavePadre  Clave del módulo padre (puede ser null).
     * @param claveModulo Clave del módulo consultado.
     * @return Optional con la proyección detallada del módulo.
     */
    public Optional<com.coralclubes.facil.modules.sistema.dto.projection.ModuloDetalleProjection> obtenerModuloPorClave(String clavePadre, String claveModulo) {
        return repo.spFacilObtenerModuloPorClave(clavePadre, claveModulo);
    }
}
