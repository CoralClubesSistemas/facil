package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPorTipoMembresiaResponse;
import com.coralclubes.facil.modules.cobranza.model.generador_movimientos.GeneracionMovimientoStrategy;
import com.coralclubes.facil.modules.cobranza.repository.GeneracionMovimientosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneracionMovimientosService {

    private final GeneracionMovimientosRepository repository;
    private final List<GeneracionMovimientoStrategy> strategies;

    public List<MovimientoPorTipoMembresiaResponse> obtenerMovimientosPorTipoMembresia(Integer tipoMembresia) {
        return repository.spCobranzaObtenerMovimientosPorTipoMembresia(tipoMembresia);
    }

    public com.coralclubes.facil.modules.cobranza.dto.response.CotizacionCredencialesResponse cotizarCredenciales(
            String membresia,
            Integer anios,
            Boolean incluirPrevios,
            Integer desarrolloConsumo
    ) {
        return repository.spCobranzaConsultarCotizacionCredenciales(membresia, anios, incluirPrevios, desarrolloConsumo);
    }

    public void generarMovimiento(GeneracionMovimientoRequest request, String usuario) {
        GeneracionMovimientoStrategy strategy = strategies.stream()
                .filter(s -> s.soporta(request.getTipoMovimientoId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró una estrategia para procesar el tipo de movimiento: " + request.getTipoMovimientoId()
                ));

        strategy.generar(request, usuario);
    }
}
