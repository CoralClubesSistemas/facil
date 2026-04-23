package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class CobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<GenerarOrdenCobranzaResponse> generarOrdenCobranzaMapper = (rs, rowNum) ->
            new GenerarOrdenCobranzaResponse(
                    rs.getInt("numeroOrden"),
                    rs.getInt("desarrolloId"),
                    rs.getObject("ordenUuid") != null ? UUID.fromString(rs.getString("ordenUuid")) : null
            );

    private final RowMapper<FinalizarOrdenCobranzaResponse> finalizarOrdenCobranzaMapper = (rs, rowNum) ->
            new FinalizarOrdenCobranzaResponse(
                    rs.getInt("numeroRecibo"),
                    rs.getInt("serieReciboId"),
                    rs.getBigDecimal("totalPagado") != null ? rs.getBigDecimal("totalPagado") : BigDecimal.ZERO
            );

    private final RowMapper<FormaPagoDto> formaPagoMapper = (rs, rowNum) ->
            new FormaPagoDto(
                    rs.getInt("id"),
                    rs.getString("clave"),
                    rs.getString("descripcion"),
                    rs.getString("icono"),
                    rs.getString("color")
            );

    private final RowMapper<DepositoCobranzaDto> depositoCobranzaMapper = (rs, rowNum) ->
            new DepositoCobranzaDto(
                    rs.getInt("idDeposito"),
                    rs.getDate("fechaOperacion") != null ? rs.getDate("fechaOperacion").toLocalDate() : null,
                    rs.getString("concepto"),
                    rs.getString("referencia"),
                    rs.getString("referenciaAmpliada"),
                    rs.getBigDecimal("importeDeposito"),
                    rs.getBigDecimal("importeDisponible"),
                    rs.getString("banco")
            );

    private final RowMapper<String> jsonStringMapper = (rs, rowNum) -> rs.getString(1);

    private final RowMapper<UUID> ordenUuidMapper = (rs, rowNum) ->
            rs.getObject("ordenUuid") != null ? UUID.fromString(rs.getString("ordenUuid")) : null;

    private final RowMapper<UUID> uuidMapper = (rs, rowNum) ->
            UUID.fromString(rs.getString("RCD_UUID"));

    private final RowMapper<RecibosCancelados> recibosCanceladosMapper = (rs, rowNum) ->
            new RecibosCancelados(
                    rs.getString("membresia"),
                    rs.getInt("numeroRecibo"),
                    rs.getInt("serieReciboId"),
                    rs.getString("serieRecibo"),
                    rs.getString("tipoRecibo"),
                    rs.getDate("fechaPago") != null ? rs.getDate("fechaPago").toLocalDate() : null,
                    rs.getBigDecimal("importe"),
                    rs.getString("estatusRecibo")
            );

    public Optional<GenerarOrdenCobranzaResponse> spCobranzaGenerarOrdenCobranza(
            String membresia,
            String usuario,
            String movimientosJson,
            Boolean agregarIVA,
            Boolean ivaIncluido
    ) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "Usuario", usuario,
                "MovimientosJSON", movimientosJson,
                "AgregarIva", agregarIVA,
                "IvaIncluido", ivaIncluido
        );

        return spExecutor.querySingle("spCobranzaGenerarOrdenCobranza", params, generarOrdenCobranzaMapper);
    }

    public Optional<FinalizarOrdenCobranzaResponse> spCobranzaFinalizarOrdenYGenerarRecibo(
            String ordenUuid,
            Integer tipoSerieRecibo,
            String usuario
    ) {
        return spExecutor.querySingle(
                "spCobranzaFinalizarOrdenYGenerarRecibo",
                Map.of(
                        "OrdenUuid", ordenUuid,
                        "TipoSerieRecibo", tipoSerieRecibo,
                        "Usuario", usuario
                ),
                finalizarOrdenCobranzaMapper
        );
    }

    public Optional<String> spFacilConsultarOrdenCobranzaJson(UUID ordenUuid) {
        return spExecutor.querySingle(
                "spFacilConsultarOrdenCobranzaJson",
                Map.of("OrdenUuid", ordenUuid),
                jsonStringMapper
        );
    }

    public Optional<UUID> spCobranzaRecuperarOrdenCobranza(Integer movimientoId, String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("MovimientoId", movimientoId);
        params.put("Membresia", membresia);

        return spExecutor.querySingle("spCobranzaRecuperarOrdenCobranza", params, ordenUuidMapper);
    }

    public Optional<String> spCobranzaObtenerDatosRecibo(Integer numeroRecibo, Integer serieReciboId) {
        return spExecutor.querySingle(
                "spCobranzaObtenerDatosRecibo",
                Map.of(
                        "NumeroRecibo", numeroRecibo,
                        "SerieReciboId", serieReciboId
                ),
                jsonStringMapper
        );
    }

    public List<FormaPagoDto> spCobranzaCatalogoFormasDePago() {
        return spExecutor.queryList("spCobranzaCatalogoFormasDePago", Collections.emptyMap(), formaPagoMapper);
    }

    public List<DepositoCobranzaDto> spCobranzaObtenerDepositos(
            Integer idBanco,
            LocalDate fechaDeposito,
            String busqueda
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdBanco", idBanco);
        params.put("fechaDeposito", fechaDeposito);
        params.put("Busqueda", busqueda);

        return spExecutor.queryList("spCobranzaObtenerDepositos", params, depositoCobranzaMapper);
    }

    // Modificación en CobranzaRepository.java
    public Optional<UUID> spCobranzaActualizarMetadatosDigitales(
            Integer numeroRecibo,
            Integer serieReciboId,
            String fileId,
            String cadenaSeguridad,
            String usuario
    ) {
        return spExecutor.querySingle(
                "spCobranzaActualizarMetadatosDigitales",
                Map.of(
                        "NumeroRecibo", numeroRecibo,
                        "SerieReciboId", serieReciboId,
                        "FileId", fileId,
                        "CadenaSeguridad", cadenaSeguridad,
                        "Usuario", usuario
                ),
                uuidMapper
        );
    }

    public void spCobranzaCancelarOrdenCobranzaSinPago(String uuid) {
        spExecutor.execute(
                "spCobranzaCancelarOrdenCobranzaSinPago",
                Map.of("OrdenUuid", uuid)
        );
    }

    public List<RecibosCancelados> spCobranzaObtenerRecibosCancelados(String membresia, String recibo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("membresia", membresia);
        params.put("recibo", recibo);

        return spExecutor.queryList(
                "spCobranzaObtenerRecibosCancelados",
                params,
                recibosCanceladosMapper
        );
    }
}
