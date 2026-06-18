package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.*;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private final RowMapper<UUID> ordenUuidMapper = (rs, rowNum) -> {
        String uuidStr = rs.getString("ordenUuid");
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    };

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

    private final RowMapper<CarteraEjecutivoResponse> carteraEjecutivoMapper = (rs, rowNum) ->
            new CarteraEjecutivoResponse(
                    rs.getObject("TotalRegistros", Integer.class),
                    rs.getString("membresia"),
                    rs.getString("nombreCompleto"),
                    rs.getString("nombre"),
                    rs.getString("segundoNombre"),
                    rs.getString("apellidoPaterno"),
                    rs.getString("apellidoMaterno"),
                    rs.getDate("fechaNacimiento") != null ? rs.getDate("fechaNacimiento").toLocalDate() : null,
                    rs.getString("correo"),
                    rs.getString("correoAlternativo"),
                    rs.getString("telefono"),
                    rs.getString("telefonoAlternativo"),
                    rs.getBigDecimal("saldoFinMes"),
                    rs.getString("tipoTarjetaAfiliada"),
                    rs.getString("ejecutivoAsignado"),
                    rs.getString("ultimoPQAPagado"),
                    rs.getBigDecimal("puntosDisponibles"),
                    rs.getBigDecimal("puntosConsumidos"),
                    rs.getObject("totalBenefActivos", Integer.class),
                    rs.getString("nombresBeneficiarios"),
                    rs.getObject("tipoMembresiaId", Integer.class),
                    rs.getString("tipoMembresia"),
                    rs.getObject("clasificacionMembresiaId", Integer.class),
                    rs.getString("clasificacionMembresia"),
                    rs.getObject("desarrolloId", Integer.class),
                    rs.getString("desarrollo"),
                    rs.getObject("estatusMembresiaId", Integer.class),
                    rs.getString("estatusMembresia"),
                    rs.getObject("carteraCobranzaId", Integer.class),
                    rs.getString("carteraCobranza"),
                    rs.getString("vigenciaOriginal"),
                    rs.getString("tiempoRestante")
            );

    public Optional<GenerarOrdenCobranzaResponse> spCobranzaGenerarOrdenCobranza(
            String membresia,
            String usuario,
            String movimientosJson,
            Boolean agregarIVA,
            Boolean ivaIncluido,
            String mensajeAdicional
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Usuario", usuario);
        params.put("MovimientosJSON", movimientosJson);
        params.put("AgregarIva", agregarIVA);
        params.put("IvaIncluido", ivaIncluido);
        params.put("MensajeAdicional", mensajeAdicional);

        return spExecutor.querySingle("spCobranzaGenerarOrdenCobranza", params, generarOrdenCobranzaMapper);
    }

    public Optional<String> spCobranzaFinalizarOrdenYGenerarRecibo(
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
                jsonStringMapper
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

    public Optional<String> spCobranzaObtenerDatosRecibo(Integer numeroRecibo, Integer serieReciboId, String membresia) {
        return spExecutor.querySingle(
                "spCobranzaObtenerDatosRecibo",
                Map.of(
                        "NumeroRecibo", numeroRecibo,
                        "SerieReciboId", serieReciboId,
                        "Membresia", membresia
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
            String busqueda,
            BigDecimal monto
    ) {
        return spExecutor.queryList("spCobranzaObtenerDepositos",
                Map.of(
                        "IdBanco", idBanco,
                        "FechaDeposito", fechaDeposito,
                        "Busqueda", busqueda,
                        "Monto", monto
                ),
                depositoCobranzaMapper);
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

    public void spCobranzaCancelarOrdenCobranzaSinPago(String uuid, String usuario) {
        spExecutor.execute(
                "spCobranzaCancelarOrdenCobranzaSinPago",
                Map.of("OrdenUuid", uuid, "Usuario", usuario)
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

    public List<CarteraEjecutivoResponse> spCobranzaObtenerCarteraEjecutivo(String usuario) {
        return spExecutor.queryList(
                "spCobranzaObtenerCarteraEjecutivo",
                Map.of("Usuario", usuario),
                carteraEjecutivoMapper
        );
    }

    public Optional<String> spClientesObtenerDataParaAnalisis(String membresia) {
        return spExecutor.querySingle(
                "spClientesObtenerDataParaAnalisis",
                Map.of("Membresia", membresia),
                jsonStringMapper
        );
    }
}
