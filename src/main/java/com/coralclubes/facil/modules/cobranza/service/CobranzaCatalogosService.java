package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.dto.SelectGenerico;
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
}
