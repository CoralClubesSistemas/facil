package com.coralclubes.facil.modules.dashboard.service;

import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesXDesarrolloResponse;
import com.coralclubes.facil.modules.dashboard.repository.DashEstadisticasVentasRepository;
import com.coralclubes.facil.modules.dashboard.dto.response.EstadisticasTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.IngresosTotalesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.facil.shared.domain.enums.DesarrolloLogoEnum;
import com.coralclubes.facil.shared.domain.enums.DesarrollosEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashEstadisticasVentasService {

    private final DashEstadisticasVentasRepository dashEstadisticasVentasRepository;

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerMembresiasTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEstadisticasVentasRepository.spDashMembresiasTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerGolfTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEstadisticasVentasRepository.spDashGolfTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerEventosTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEstadisticasVentasRepository.spDashEventosTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<TotalCategoriaResponse<Long>> obtenerInmobiliariaTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return dashEstadisticasVentasRepository.spDashInmobiliariaTotalesVentas(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public Optional<IngresosTotalesResponse> obtenerIngresosTotalesVentas(LocalDate fechaInicio, LocalDate fechaFin, Integer desarrollo) {
        return dashEstadisticasVentasRepository.spDashBoardIngresosTotalesVentas(fechaInicio, fechaFin, desarrollo);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesResponse> obtenerEstadisticasTotalesVentas(Integer anio) {
        return dashEstadisticasVentasRepository.spDashEstadisticasTotalesVentas(anio);
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesXDesarrolloResponse> obtenerEstadisticasTotalesXDesarrolloVentas(Integer anio, Integer desarrollo) {
        List<DesarrollosEnum> desarrollos = (desarrollo == null || desarrollo == 0)
                ? List.of(DesarrollosEnum.values())
                : List.of(DesarrollosEnum.fromId(desarrollo));

        return desarrollos.stream()
                .map(d -> new EstadisticasTotalesXDesarrolloResponse(
                        d.getId(),
                        d.getNombre(),
                        dashEstadisticasVentasRepository.spDashEstadisticasTotalesXDesarrolloVentas(anio, d.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EstadisticasTotalesXDesarrolloResponse> obtenerCrecimientoMensualXDesarrolloVentas(Integer anio, Integer desarrollo) {
        List<EstadisticasTotalesXDesarrolloResponse> desarrollosTotales =
                obtenerEstadisticasTotalesXDesarrolloVentas(anio, desarrollo);

        return desarrollosTotales.stream()
                .map(d -> new EstadisticasTotalesXDesarrolloResponse(
                        d.idDesarrollo(),
                        d.desarrollo(),
                        calcularCrecimientoIterativo(d.estadisticas())
                ))
                .toList();
    }

    // =========================================================================
    // HELPER PRIVADO RECURSIVO
    // =========================================================================

    private List<EstadisticasTotalesResponse> calcularCrecimientoIterativo(
            List<EstadisticasTotalesResponse> estadisticas
    ) {
        if (estadisticas == null || estadisticas.isEmpty()) {
            return List.of();
        }

        List<EstadisticasTotalesResponse> resultado = new java.util.ArrayList<>();
        BigDecimal totalPrevio = null;

        for (EstadisticasTotalesResponse actual : estadisticas) {
            BigDecimal totalActual = actual.total() != null ? actual.total() : BigDecimal.ZERO;
            BigDecimal porcentajeCrecimiento;

            if (totalPrevio == null) {
                porcentajeCrecimiento = BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
            } else if (totalPrevio.compareTo(BigDecimal.ZERO) == 0) {
                porcentajeCrecimiento = totalActual.compareTo(BigDecimal.ZERO) > 0
                        ? new BigDecimal("100.00")
                        : BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
            } else {
                porcentajeCrecimiento = totalActual.subtract(totalPrevio)
                        .divide(totalPrevio, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
            }

            resultado.add(new EstadisticasTotalesResponse(
                    actual.periodo(),
                    actual.periodoNombre(),
                    porcentajeCrecimiento
            ));

            totalPrevio = totalActual;
        }

        return resultado;
    }
}
