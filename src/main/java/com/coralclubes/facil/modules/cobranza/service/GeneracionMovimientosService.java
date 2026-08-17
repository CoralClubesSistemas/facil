package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPorTipoMembresiaResponse;
import com.coralclubes.facil.modules.cobranza.repository.GeneracionMovimientosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneracionMovimientosService {

    private final GeneracionMovimientosRepository repository;

    public List<MovimientoPorTipoMembresiaResponse> obtenerMovimientosPorTipoMembresia(Integer tipoMembresia) {
        return repository.spCobranzaObtenerMovimientosPorTipoMembresia(tipoMembresia);
    }
}
