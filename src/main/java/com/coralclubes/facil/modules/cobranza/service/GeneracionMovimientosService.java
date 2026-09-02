package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.GeneracionMovimientoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CotizacionCredencialesResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MapeoPeriodicidadResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoManualResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.MovimientoPorTipoMembresiaResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.TarifaMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.UltimoMovimientoResponse;
import com.coralclubes.facil.modules.cobranza.model.generador_movimientos.GeneracionMovimientoStrategy;
import com.coralclubes.facil.modules.cobranza.repository.GeneracionMovimientosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeneracionMovimientosService {

    private final GeneracionMovimientosRepository repository;
    private final List<GeneracionMovimientoStrategy> strategies;

    public List<MovimientoPorTipoMembresiaResponse> obtenerMovimientosPorTipoMembresia(String membresia) {
        return repository.spCobranzaObtenerMovimientosPorTipoMembresia(membresia);
    }

    public CotizacionCredencialesResponse cotizarCredenciales(
            String membresia,
            Integer anios,
            Boolean incluirPrevios,
            Integer desarrolloConsumo
    ) {
        return repository.spCobranzaConsultarCotizacionCredenciales(membresia, anios, incluirPrevios, desarrolloConsumo);
    }

    public List<MovimientoManualResponse> generarMovimiento(GeneracionMovimientoRequest request, String usuario) {
        GeneracionMovimientoStrategy strategy = strategies.stream()
                .filter(s -> s.soporta(request.getTipoMovimientoId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró una estrategia para procesar el tipo de movimiento: " + request.getTipoMovimientoId()
                ));

        return strategy.generar(request, usuario);
    }

    public Optional<UltimoMovimientoResponse> obtenerUltimoMovimiento(
            String membresia,
            Integer desarrolloConsumo,
            Integer tipoMovimiento,
            Integer estatus
    ) {
        return repository.spCobranzaObtenerUltimoMovimiento(membresia, desarrolloConsumo, tipoMovimiento, estatus);
    }

    public List<MapeoPeriodicidadResponse> obtenerMapeoPeriodicidad(Integer periodicidad, String periodo) {
        return repository.spCobranzaMapeoPeriodicidad(periodicidad, periodo);
    }

    public Optional<TarifaMovimientoResponse> obtenerTarifaMovimiento(
            String membresia,
            Integer tipoMovimiento,
            Integer anio
    ) {
        return repository.spCobranzaObtenerTarifaMovimiento(membresia, tipoMovimiento, anio);
    }
}
