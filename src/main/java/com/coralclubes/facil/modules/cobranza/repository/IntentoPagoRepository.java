package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.IntentoPagoDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IntentoPagoRepository {
    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<Integer> idIntentoMapper = (rs, rowNum) -> rs.getInt("intentoPagoId");

    private final RowMapper<IntentoPagoDto> intentoPagoRowMapper = (rs, rowNum) -> IntentoPagoDto.builder()
            .intentoPagoId(rs.getInt("intentoPagoId"))
            .formaPagoClave(rs.getString("formaPagoClave"))
            .formaPagoDescripcion(rs.getString("formaPagoDescripcion"))
            .icono(rs.getString("icono"))
            .color(rs.getString("color"))
            .monto(rs.getBigDecimal("monto"))
            .estatus(rs.getString("estatus"))
            .metadata(rs.getString("metadata"))
            .fechaCreacion(rs.getTimestamp("fechaCreacion") != null ? rs.getTimestamp("fechaCreacion").toLocalDateTime() : null)
            .fechaAprobacion(rs.getTimestamp("fechaAprobacion") != null ? rs.getTimestamp("fechaAprobacion").toLocalDateTime() : null)
            .build();

    /**
     * Registra un nuevo intento de pago en la base de datos.
     * Retorna el ID autogenerado.
     */
    public Optional<Integer> spCobranzaRegistrarIntentoPago(
            UUID ordenUuid,
            String formaPagoClave,
            BigDecimal monto,
            String estatus,
            String metadata
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("OrdenUuid", ordenUuid.toString());
        params.put("FormaPagoClave", formaPagoClave);
        params.put("Monto", monto);
        params.put("Estatus", estatus);
        params.put("Metadata", metadata);

        return spExecutor.querySingle("spCobranzaRegistrarIntentoPago", params, idIntentoMapper);
    }

    /**
     * Actualiza el estatus de un intento de pago (Ej. de PENDIENTE a APROBADO)
     */
    public void spCobranzaActualizarEstatusIntentoPago(
            Integer intentoPagoId,
            String nuevoEstatus,
            LocalDateTime fechaAprobacion
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("IntentoPagoId", intentoPagoId);
        params.put("NuevoEstatus", nuevoEstatus);
        params.put("FechaAprobacion", fechaAprobacion);

        spExecutor.execute("spCobranzaActualizarEstatusIntentoPago", params);
    }

    /**
     * Obtiene el historial de intentos de pago de una orden específica.
     */
    public List<IntentoPagoDto> spCobranzaObtenerIntentosPagoPorOrden(UUID ordenUuid) {
        Map<String, Object> params = new HashMap<>();
        params.put("OrdenUuid", ordenUuid.toString());

        return spExecutor.queryList("spCobranzaObtenerIntentosPagoPorOrden", params, intentoPagoRowMapper);
    }

    /**
     * Elimina un intento de pago si la orden aún no ha sido completada.
     */
    public void spCobranzaEliminarIntentoPago(UUID ordenUuid, Integer intentoPagoId) {
        Map<String, Object> params = new HashMap<>();
        params.put("OrdenUuid", ordenUuid.toString());
        params.put("IntentoPagoId", intentoPagoId);

        spExecutor.execute("spCobranzaEliminarIntentoPago", params);
    }

    public void spCobranzaRegistrarPagoEfectivo(Integer IdIntentoPago) {
        spExecutor.execute("spCobranzaRegistrarPagoEfectivo", Map.of("IdIntentoPago", IdIntentoPago));
    }

    public void spCobranzaRegistrarPagoTarjeta(Integer IdIntentoPago) {
        spExecutor.execute("spCobranzaRegistrarPagoTarjeta", Map.of("IdIntentoPago", IdIntentoPago));
    }

    public void spCobranzaRegistrarPagoDeposito(Integer IdIntentoPago) {
        spExecutor.execute("spCobranzaRegistrarPagoDeposito", Map.of("IdIntentoPago", IdIntentoPago));
    }

    public void spCobranzaRegistrarPagoLink(Integer IdIntentoPago) {
        spExecutor.execute("spCobranzaRegistrarPagoLink", Map.of("IdIntentoPago", IdIntentoPago));
    }

    public void spCobranzaRegistrarPagoSustitucionRecibo(Integer IdIntentoPago) {
        spExecutor.execute("spCobranzaRegistrarPagoSustitucionRecibo", Map.of("IdIntentoPago", IdIntentoPago));
    }
}