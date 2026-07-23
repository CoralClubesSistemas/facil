package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCatalogoElementoResponse;
import com.coralclubes.facil.modules.cobranza.repository.CuponesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CuponesService {

    private final CuponesRepository repository;

    public List<CuponesCatalogoElementoResponse> obtenerCatalogoCondiciones() {
        return repository.spCuponesCatalogoCondiciones();
    }

    public List<CuponesCatalogoElementoResponse> obtenerCatalogoBeneficios() {
        return repository.spCuponesCatalogoBeneficios();
    }

    public List<SelectGenerico<Integer>> obtenerCatalogoOrigenes() {
        return repository.spCuponesCatalogoOrigenes();
    }

    public List<SelectGenerico<Integer>> obtenerCatalogoDestinos() {
        return repository.spCuponesCatalogoDestinos();
    }

    public Integer guardarCupon(GuardarCuponRequest request) {
        return repository.spCuponesGuardarCupon(request)
                .orElseThrow(() -> new RuntimeException("No se pudo guardar el cupón"));
    }
}
