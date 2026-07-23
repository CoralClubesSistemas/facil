package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.enums.CatalogoCobranzaEnum;
import com.coralclubes.facil.modules.cobranza.repository.CobranzaCatalogosRepository;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CobranzaCatalogosService {
    private final CobranzaCatalogosRepository repository;

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoTiposSeries() {
        return ApiResponse.success(repository.spCobranzaCatalogoTiposSeries());
    }

    public ApiResponse<BigDecimal> obtenerPorcentajeAutorizado(Integer idDesarrollo, Integer clasificacionMembresia) {
        return ApiResponse.success(repository.spCobranzaObtenerPorcentajeLimite(idDesarrollo, clasificacionMembresia));
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoTerminales() {
        return ApiResponse.success(repository.spCobranzaCatalogoTerminales());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoBancos() {
        return ApiResponse.success("Bancos obtenidos correctamente.", repository.spCobranzaCatalogoBancos());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoTiposMovimientos() {
        return ApiResponse.success("Catálogo de tipos de movimientos obtenido correctamente.", repository.spCobranzaCatalogoTiposMovimientos());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoEstatusMovimientos() {
        return ApiResponse.success("Catálogo de estatus de movimientos obtenido correctamente.", repository.spCobranzaCatalogoEstatusMovimientos());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoDesarrollos() {
        return ApiResponse.success("Catálogo de desarrollos obtenido correctamente.", repository.spCobranzaCatalogoDesarrollos());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoTiposMembresias(Integer idClasificacion, Integer idDesarrollo) {
        return ApiResponse.success("Catálogo de desarrollos obtenido correctamente.", repository.spRepoCatalogoTiposMembresias(idClasificacion, idDesarrollo));
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoEstatusPuntos() {
        return ApiResponse.success("Catálogo de estatus de puntos obtenido correctamente.", repository.spCobranzaCatalogoEstatusPuntos());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoPorClave(String clave) {
        CatalogoCobranzaEnum catalogoEnum = CatalogoCobranzaEnum.fromClave(clave);
        List<SelectGenerico<Integer>> resultado = repository.obtenerCatalogoPorSp(catalogoEnum.getSpName());
        return ApiResponse.success("Catálogo de " + catalogoEnum.getClave() + " obtenido correctamente.", resultado);
    }
}
