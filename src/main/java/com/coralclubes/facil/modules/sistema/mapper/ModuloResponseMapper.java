package com.coralclubes.facil.modules.sistema.mapper;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDtoResult;
import com.coralclubes.facil.modules.sistema.dto.response.ModuloApiResponse;

import java.util.ArrayList;
import java.util.function.Function;

public class ModuloResponseMapper implements Function<ModuloDtoResult, ModuloApiResponse> {

    @Override
    public ModuloApiResponse apply(ModuloDtoResult source) {
        if (source == null) {
            return null;
        }

        return ModuloApiResponse.builder()
                .id(source.id())
                .idPadre(source.idPadre())
                .clave(source.clave())
                .nombre(source.nombre())
                .ruta(source.ruta())
                .icono(source.icono())
                .menuFacil(source.menuFacil())
                .orden(source.orden())
                .hijos(new ArrayList<>()) // Inicializa la lista de hijos como vacía
                .build();
    }

    // Mantener compatibilidad con llamadas explícitas .map() si existieran
    public ModuloApiResponse map(ModuloDtoResult source) {
        return apply(source);
    }
}
