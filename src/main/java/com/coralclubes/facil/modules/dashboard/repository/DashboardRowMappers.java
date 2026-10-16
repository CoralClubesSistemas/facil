package com.coralclubes.facil.modules.dashboard.repository;

import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public final class DashboardRowMappers {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

    // patron singleton para evitar instanciación
    private DashboardRowMappers() {
    }

    public static String formatFecha(java.time.LocalDate fecha) {
        return fecha != null ? fecha.format(DATE_FORMATTER) : null;
    }

    public static <T> RowMapper<TotalCategoriaResponse<T>> crearMapper(Class<T> tipoId, String columnaConteo) {
        return (rs, rowNum) -> new TotalCategoriaResponse<>(
                rs.getObject("id", tipoId),
                rs.getString("categoria"),
                rs.getObject(columnaConteo) != null ? rs.getInt(columnaConteo) : null,
                rs.getBigDecimal("total"),
                extraerMetadata(rs)
        );
    }

    public static <T> RowMapper<TotalCategoriaResponse<T>> crearMapper(Class<T> tipoId) {
        return (rs, rowNum) -> new TotalCategoriaResponse<>(
                rs.getObject("id", tipoId),
                rs.getString("categoria"),
                rs.getObject("totalMembresias") != null ? rs.getInt("totalMembresias") : null,
                rs.getBigDecimal("total"),
                extraerMetadata(rs)
        );
    }

    public static RowMapper<TotalCategoriaResponse<Long>> crearMapper() {
        return (rs, rowNum) -> new TotalCategoriaResponse<>(
                rs.getObject("id", Long.class),
                rs.getString("categoria"),
                rs.getObject("totalMembresias") != null ? rs.getInt("totalMembresias") : null,
                rs.getBigDecimal("total"),
                extraerMetadata(rs)
        );
    }

    public static RowMapper<TotalCategoriaResponse<String>> crearMapperString(String columnaConteo) {
        return (rs, rowNum) -> new TotalCategoriaResponse<>(
                rs.getString("id"),
                rs.getString("categoria"),
                rs.getObject(columnaConteo) != null ? rs.getInt(columnaConteo) : null,
                rs.getBigDecimal("total"),
                extraerMetadata(rs)
        );
    }

    public static RowMapper<TotalCategoriaResponse<String>> crearMapperString() {
        return (rs, rowNum) -> new TotalCategoriaResponse<>(
                rs.getString("id"),
                rs.getString("categoria"),
                rs.getObject("totalMembresias") != null ? rs.getInt("totalMembresias") : null,
                rs.getBigDecimal("total"),
                extraerMetadata(rs)
        );
    }

    private static Map<String, Object> extraerMetadata(ResultSet rs) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                if ("metadata".equalsIgnoreCase(metaData.getColumnLabel(i)) || "metadata".equalsIgnoreCase(metaData.getColumnName(i))) {
                    String json = rs.getString(i);
                    if (json != null && !json.isBlank()) {
                        return OBJECT_MAPPER.readValue(json, MAP_TYPE_REF);
                    }
                    return null;
                }
            }
        } catch (Exception ignored) {
            // Si la columna no existe o no es un JSON válido, se retorna null
        }
        return null;
    }
}
