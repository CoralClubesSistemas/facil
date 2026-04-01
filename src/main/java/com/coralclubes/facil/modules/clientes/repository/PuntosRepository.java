package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.request.ConsumoPuntosRequest;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosMembresia;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PuntosRepository {
    private final StoredProcedureExecutor executor;

    private RowMapper<PuntosMembresia> puntosMembresiaRowMapper = (rs, rowNum) -> PuntosMembresia.builder()
            .membresia(rs.getString("Membresia"))
            .nombreSocio(rs.getString("NombreSocio"))
            .puntosLiberadosRegulares(rs.getInt("PuntosLiberadosRegulares"))
            .puntosLiberadosPromocion(rs.getInt("PuntosLiberadosPromocion"))
            .totalPuntosLiberados(rs.getInt("TotalPuntosLiberados"))
            .puntosConsumidos(rs.getInt("PuntosConsumidos"))
            .saldoPuntosNeto(rs.getInt("SaldoPuntosNeto"))
            .fechaEmisionReporte(rs.getTimestamp("FechaEmisionReporte"))
            .build();

    private final RowMapper<Integer> folioGeneradoMapper = (rs, rowNum) -> rs.getInt("FolioPuntosGenerado");

    public Integer spCliConsumirPuntos(ConsumoPuntosRequest request) {
        Map<String, Object> params = new HashMap<>();

        params.put("Membresia", request.membresia());
        params.put("DesarrolloId", request.desarrolloId());
        params.put("TotalPuntos", request.totalPuntos());
        params.put("PuntosHospedaje", request.puntosHospedaje() != null ? request.puntosHospedaje() : 0);
        params.put("PuntosInstalaciones", request.puntosInstalaciones() != null ? request.puntosInstalaciones() : 0);
        params.put("PuntosCampoGolf", request.puntosCampoGolf() != null ? request.puntosCampoGolf() : 0);
        params.put("ImportePuntos", 0);
        params.put("IdMovimiento", request.idMovimiento());
        params.put("Descripcion", request.descripcion());
        params.put("Usuario", request.usuario());
        params.put("NumBeneficiario", request.numBeneficiario() != null ? request.numBeneficiario() : 1);
        params.put("IdTipoCliente", request.idTipoCliente() != null ? request.idTipoCliente() : 0);
        params.put("IdTipoAcceso", request.idTipoAcceso() != null ? request.idTipoAcceso() : 0);
        params.put("IdPeriodoUso", request.idPeriodoUso() != null ? request.idPeriodoUso() : 0);

        return executor.querySingleLog("spCliConsumirPuntos", params, folioGeneradoMapper, request.usuario(), true, true)
                .orElseThrow(() -> new RuntimeException("No se pudo generar el folio de consumo de puntos en BD."));
    }

    public PuntosMembresia spSaldoPuntosDisponiblesMembresia(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);

        return executor.querySingle("spSaldoPuntosDisponiblesMembresia", params, puntosMembresiaRowMapper)
                .orElse(null);
    }
}
