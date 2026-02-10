package com.coralclubes.facil.modules.sistema.service;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.modules.sistema.dto.response.ModuloApiResponse;
import com.coralclubes.facil.modules.sistema.repository.ModulosRepository;
import com.coralclubes.responses.ApiResponse;
import com.coralclubes.responses.BaseResponseCode;
import com.coralclubes.responses.codes.GeneralResponseCode;
import lombok.Getter;
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

        return ApiResponse.success(
                getFormatModules(allModules)
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
     * Lógica de Negocio: Construcción del Árbol de Módulos.
     * Convierte una lista plana en una estructura jerárquica (Padre -> Hijos).
     */
    public List<ModuloApiResponse> getFormatModules(List<ModuloDtoResult> allModules) {
        if (allModules == null || allModules.isEmpty()) return new ArrayList<>();

        // 1. Mapeo ID -> Objeto Respuesta
        Map<Integer, ModuloApiResponse> map = new HashMap<>();
        allModules.forEach(mod -> map.put(mod.id(), convertToApiResponse(mod)));

        // 2. Construcción de Jerarquía
        List<ModuloApiResponse> rootModules = new ArrayList<>();

        map.values().forEach(modulo -> {
            Integer idPadre = modulo.getIdPadre();

            // Regla de Negocio: El ID 10 se considera la raíz absoluta en este sistema
            if (idPadre != null && idPadre == 10) {
                rootModules.add(modulo);
            } else if (idPadre != null && map.containsKey(idPadre)) {
                // Si tiene padre y el padre existe en la lista, lo agregamos como hijo
                map.get(idPadre).getHijos().add(modulo);
            }
            // Si el padre no es 10 y no está en el mapa, es un módulo huérfano o raíz alternativa.
            // Dependiendo de la regla de negocio, se podría agregar a rootModules o ignorar.
        });

        // 3. Asignación de niveles (Recursivo)
        rootModules.forEach(root -> asignNivelRecursivo(root, 0));

        // 4. Ordenamiento Final por ID
        rootModules.sort(Comparator.comparing(ModuloApiResponse::getId));

        return rootModules;
    }

    /**
     * Método recursivo para etiquetar la profundidad del árbol.
     */
    private void asignNivelRecursivo(ModuloApiResponse modulo, int depth) {
        String nivelLabel = switch (depth) {
            case 0 -> "PADRE";
            case 1 -> "HIJO";
            case 2 -> "NIETO";
            default -> "NIVEL_" + depth;
        };

        modulo.setNivel(nivelLabel);

        if (modulo.getHijos() != null && !modulo.getHijos().isEmpty()) {
            // Ordenar hijos antes de procesarlos
            modulo.getHijos().sort(Comparator.comparing(ModuloApiResponse::getId));
            // Recursión
            modulo.getHijos().forEach(hijo -> asignNivelRecursivo(hijo, depth + 1));
        }
    }

    /**
     * Mapper manual DTO -> Response.
     * (Podría moverse a una clase Mapper separada con MapStruct, pero aquí es válido por simplicidad)
     */
    private ModuloApiResponse convertToApiResponse(ModuloDtoResult modulo) {
        return ModuloApiResponse.builder()
                .id(modulo.id())
                .idPadre(modulo.idPadre())
                .clave(modulo.clave())
                .nombre(modulo.nombre())
                .ruta(modulo.ruta())
                .icono(modulo.icono())
                .menuFacil(modulo.menuFacil())
                .hijos(new ArrayList<>()) // Inicializamos lista vacía para evitar NullPointer
                .build();
    }
}
