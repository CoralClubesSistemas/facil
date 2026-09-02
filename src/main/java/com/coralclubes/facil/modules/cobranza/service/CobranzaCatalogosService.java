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

    public List<SelectGenerico<Integer>> obtenerCatalogoTiposMovimientos(Integer tipoMembresia) {
        return repository.spCobranzaCatalogoTiposMovimientos(tipoMembresia);
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

    public List<SelectGenerico<Integer>> obtenerCatalogoClasificacionesMembresias(Integer idDesarrollo) {
        return repository.spCobranzaCatalogoClasificacionesMembresias(idDesarrollo);
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoPorClave(String clave) {
        CatalogoCobranzaEnum catalogoEnum = CatalogoCobranzaEnum.fromClave(clave);
        List<SelectGenerico<Integer>> resultado = repository.obtenerCatalogoPorSp(catalogoEnum.getSpName());
        return ApiResponse.success("Catálogo de " + catalogoEnum.getClave() + " obtenido correctamente.", resultado);
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogoMotivosAccesoPreferencial() {
        return ApiResponse.success("Catálogo de motivos de acceso preferencial obtenido correctamente.", repository.spCobranzaCatalogoMotivosAccesoPreferencial());
    }

    public ApiResponse<List<SelectGenerico<String>>> obtenerCatalogoEsquemasPagoPaqueteAnual() {
        return ApiResponse.success("Catálogo de esquemas de pago de paquete anual obtenido correctamente.", repository.spCobranzaCatalogoEsquemasPagoPaqueteAnual());
    }
}
