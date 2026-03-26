package com.coralclubes.facil.modules.reportes.repository;

import com.coralclubes.facil.modules.reportes.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReportesMotorRepository {

    @Qualifier("replicaJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter FORMATO_ENTRADA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // =========================================================================
    // MAPPERS (Catálogos y Motor)
    // =========================================================================

    private final RowMapper<ReporteDisponibleDto> reporteMapper = (rs, rowNum) -> ReporteDisponibleDto.builder()
            .idReporte(rs.getInt("IdReporte"))
            .nombreReporte(rs.getString("NombreReporte"))
            .build();

    private final RowMapper<ParametroReporteDto> parametroMapper = (rs, rowNum) -> ParametroReporteDto.builder()
            .idParametro(rs.getInt("IdParametro"))
            .nombreFiltroUI(rs.getString("NombreFiltroUI"))
            .endpointData(rs.getString("EndpointData"))
            .build();

    private final RowMapper<ProcedimientoEjecucionDto> procedimientoMapper = (rs, rowNum) -> ProcedimientoEjecucionDto.builder()
            .nombreStoredProcedure(rs.getString("NombreStoredProcedure"))
            .totalParametrosEsperados(rs.getInt("TotalParametrosEsperados"))
            .build();

    private final RowMapper<FavoritoReporteDto> favoritoMapper = (rs, rowNum) -> FavoritoReporteDto.builder()
            .idFavorito(rs.getInt("IdFavorito"))
            .idParametroFiltro(rs.getInt("IdParametroFiltro"))
            .valorSeleccionado(rs.getInt("ValorSeleccionado"))
            .build();

    private final RowMapper<ColumnaReporteDto> columnaMapper = (rs, rowNum) -> ColumnaReporteDto.builder()
            .nombreColumna(rs.getString("NombreColumna"))
            .orden(rs.getInt("Orden"))
            .visible(true) // Si está en la tabla de memoria, por definición es visible
            .build();

    private final RowMapper<ColumnaMetadataDto> metadataMapper = (rs, rowNum) -> ColumnaMetadataDto.builder()
            .nombreColumnaDB(rs.getString("NombreColumnaDB"))
            .ordenOriginal(rs.getInt("OrdenOriginal"))
            .tipoDato(rs.getString("TipoDato"))
            .build();

    private final RowMapper<ParametroMapeoDto> mapeoMapper = (rs, rowNum) -> ParametroMapeoDto.builder()
            .posicion(rs.getInt("Posicion"))
            .rol(rs.getString("Rol"))
            .nombreJava(rs.getString("NombreJava"))
            .nombreSP(rs.getString("NombreSP"))
            .tipoDato(rs.getString("TipoDato"))
            .longitud(rs.getInt("Longitud"))
            .esObligatorio(rs.getBoolean("EsObligatorio"))
            .build();

    // =========================================================================
    // CONSULTAS DEL MOTOR
    // =========================================================================

    public List<ReporteDisponibleDto> obtenerReportesActivos(Integer idRol, String claveModulo) {
        return querySP("spRepoObtenerReportesActivos",
                Map.of("IdRol", idRol, "ClaveModulo", claveModulo),
                reporteMapper);
    }

    public List<ParametroReporteDto> obtenerParametrosReporte(Integer idTipoReporte) {
        return querySP("spRepoObtenerParametrosReporte",
                Map.of("IdTipoReporte", idTipoReporte),
                parametroMapper);
    }

    public ProcedimientoEjecucionDto obtenerProcedimientoEjecucion(Integer idTipoReporte) {
        List<ProcedimientoEjecucionDto> result = querySP("spRepoObtenerProcedimientoEjecucion",
                Map.of("IdTipoReporte", idTipoReporte),
                procedimientoMapper);
        return result.isEmpty() ? null : result.getFirst();
    }

    public List<ParametroMapeoDto> obtenerParametrosMapeo(Integer idTipoReporte) {
        return querySP("spReporteObtenerParametrosMapeo",
                Map.of("IdTipoReporte", idTipoReporte),
                mapeoMapper);
    }

    // =========================================================================
    // GESTIÓN DE PREFERENCIAS (Favoritos y Columnas)
    // =========================================================================

    public List<FavoritoReporteDto> obtenerFavoritosUsuario(Integer idTipoReporte, String usuario) {
        return querySP("spRepoObtenerFavoritosUsuario",
                Map.of("IdTipoReporte", idTipoReporte, "Usuario", usuario),
                favoritoMapper);
    }

    public void guardarFavoritosUsuario(Integer idTipoReporte, String usuario, Integer idParametroReporte, String valoresCSV, Integer activo) {
        executeSP("spRepoGuardarFavoritosUsuario",
                Map.of("IdTipoReporte", idTipoReporte,
                        "Usuario", usuario,
                        "IdParametroReporte", idParametroReporte,
                        "ValoresCSV", valoresCSV,
                        "Activo", activo));
    }

    public List<ColumnaReporteDto> obtenerColumnasUsuario(Integer idTipoReporte, String usuario) {
        return querySP("spRepoObtenerColumnasUsuario",
                Map.of("IdTipoReporte", idTipoReporte, "Usuario", usuario),
                columnaMapper);
    }

    public void guardarColumnasUsuarioMasivo(Integer idTipoReporte, String usuario, String columnasJSON) {
        executeSP("spRepoGuardarColumnasUsuarioMasivo",
                Map.of("IdTipoReporte", idTipoReporte,
                        "Usuario", usuario,
                        "ColumnasJSON", columnasJSON));
    }

    // =========================================================================
    // EJECUCIÓN DE REPORTES Y METADATA JDBC
    // =========================================================================

    /**
     * Ejecuta el SP con parámetros NULL para obtener la metadata real de las columnas
     * del ResultSet. Lee el ResultSetMetaData directamente desde JDBC.
     */
    public List<ColumnaMetadataDto> obtenerMetadataColumnas(String nombreSP, List<ParametroMapeoDto> mapeo) {
        mapeo.sort(Comparator.comparing(ParametroMapeoDto::posicion));
        String callString = construirCallString(nombreSP, mapeo.size());
        return jdbcTemplate.execute(
                (Connection con) -> {
                    try (CallableStatement cs = con.prepareCall(callString)) {
                        for (int i = 1; i <= mapeo.size(); i++) {
                            cs.setNull(i, Types.VARCHAR);
                        }
                        boolean hasResult = cs.execute();
                        if (hasResult) {
                            try (ResultSet rs = cs.getResultSet()) {
                                ResultSetMetaData metaData = rs.getMetaData();
                                List<ColumnaMetadataDto> columnas = new ArrayList<>();
                                for (int j = 1; j <= metaData.getColumnCount(); j++) {
                                    columnas.add(ColumnaMetadataDto.builder()
                                            .nombreColumnaDB(metaData.getColumnLabel(j))
                                            .ordenOriginal(j)
                                            .tipoDato(metaData.getColumnTypeName(j))
                                            .build());
                                }
                                return columnas;
                            }
                        }
                        return Collections.emptyList();
                    }
                }
        );
    }

    public List<Map<String, Object>> ejecutarReporteMapeado(String nombreSP, List<ParametroMapeoDto> mapeo, Map<String, Object> parametrosJava) {
        mapeo.sort(Comparator.comparing(ParametroMapeoDto::posicion));
        String callString = construirCallString(nombreSP, mapeo.size());

        return jdbcTemplate.execute(
                (Connection con) -> {
                    try (CallableStatement cs = con.prepareCall(callString)) {
                        asignarParametros(cs, mapeo, parametrosJava);
                        boolean hasResult = cs.execute();
                        if (hasResult) {
                            try (ResultSet rs = cs.getResultSet()) {
                                return procesarResultSet(rs);
                            }
                        }
                        return Collections.emptyList();
                    }
                }
        );
    }

    // =========================================================================
    // UTILERÍAS INTERNAS DE JDBC
    // =========================================================================

    private void asignarParametros(CallableStatement cs, List<ParametroMapeoDto> mapeo, Map<String, Object> parametrosJava) throws SQLException {
        for (int i = 0; i < mapeo.size(); i++) {
            ParametroMapeoDto param = mapeo.get(i);
            int index = i + 1;
            Object valorJava = parametrosJava.get(param.nombreJava());

            switch (param.tipoDato().toLowerCase()) {
                case "datetime", "date" -> {
                    Timestamp ts = convertirATimestamp(valorJava);
                    if (ts == null) cs.setNull(index, Types.TIMESTAMP);
                    else cs.setTimestamp(index, ts);
                }
                case "int" -> {
                    Integer val = convertirAEntero(valorJava);
                    if (val == null) cs.setNull(index, Types.INTEGER);
                    else cs.setInt(index, val);
                }
                default -> {
                    String val = convertirAString(valorJava);
                    if (val == null) cs.setNull(index, Types.VARCHAR);
                    else cs.setString(index, val);
                }
            }
        }
    }

    private List<Map<String, Object>> procesarResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        int[] sqlTypes = new int[columnCount];
        String[] columnNames = new String[columnCount];
        for (int j = 0; j < columnCount; j++) {
            sqlTypes[j] = metaData.getColumnType(j + 1);
            columnNames[j] = metaData.getColumnLabel(j + 1);
        }

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int j = 0; j < columnCount; j++) {
                row.put(columnNames[j], leerColumnaSegura(rs, j + 1, sqlTypes[j]));
            }
            rows.add(row);
        }
        return rows;
    }

    private Object leerColumnaSegura(ResultSet rs, int colIndex, int sqlType) {
        try {
            return switch (sqlType) {
                case Types.TIMESTAMP, Types.DATE, Types.TIME -> rs.getString(colIndex);
                case Types.DECIMAL, Types.NUMERIC -> {
                    BigDecimal val = rs.getBigDecimal(colIndex);
                    yield rs.wasNull() ? null : val;
                }
                case Types.INTEGER, Types.SMALLINT, Types.TINYINT -> {
                    int val = rs.getInt(colIndex);
                    yield rs.wasNull() ? null : val;
                }
                case Types.BIT, Types.BOOLEAN -> {
                    boolean val = rs.getBoolean(colIndex);
                    yield rs.wasNull() ? null : val;
                }
                default -> rs.getString(colIndex);
            };
        } catch (SQLException e) {
            try {
                return rs.getString(colIndex);
            } catch (SQLException ex) {
                return null;
            }
        }
    }

    private Timestamp convertirATimestamp(Object valor) {
        switch (valor) {
            case null -> {
                return null;
            }
            case Timestamp ts -> {
                return ts;
            }
            case java.util.Date date -> {
                return new Timestamp(date.getTime());
            }
            case String str -> {
                String fecha = str.length() >= 10 ? str.substring(0, 10) : str.trim();
                if (fecha.isBlank()) return null;
                try {
                    return Timestamp.valueOf(LocalDate.parse(fecha, FORMATO_ENTRADA).atStartOfDay());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Formato de fecha inválido: '" + valor + "'. Se esperaba yyyy-MM-dd.", e);
                }
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Tipo no soportado para fecha: " + valor.getClass().getName());
    }

    private Integer convertirAEntero(Object valor) {
        if (valor == null) return null;
        if (valor instanceof Number n) return n.intValue();
        String str = valor.toString().trim();
        if (str.isBlank()) return null;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor no convertible a entero: '" + valor + "'.", e);
        }
    }

    private String convertirAString(Object valor) {
        if (valor == null) return null;
        String str = valor.toString().trim();
        return str.isBlank() ? null : str;
    }

    private String construirCallString(String nombreSP, int cantidadParams) {
        StringBuilder sb = new StringBuilder("{call ").append(nombreSP).append("(");
        for (int i = 0; i < cantidadParams; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.append(")}").toString();
    }

    private <T> List<T> querySP(String spName, Map<String, Object> params, RowMapper<T> mapper) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(spName)
                .returningResultSet("result", mapper);
        Map<String, Object> result = jdbcCall.execute(new MapSqlParameterSource(params));
        @SuppressWarnings("unchecked")
        List<T> rows = (List<T>) result.getOrDefault("result", List.of());
        return rows;
    }

    private void executeSP(String spName, Map<String, Object> params) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(spName);
        jdbcCall.execute(new MapSqlParameterSource(params));
    }
}