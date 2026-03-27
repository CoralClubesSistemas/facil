package com.coralclubes.facil.modules.reportes.service;

import com.coralclubes.facil.modules.reportes.dto.request.EjecutarReporteRequest;
import com.coralclubes.facil.modules.reportes.dto.response.*;
import com.coralclubes.facil.modules.reportes.enums.ClavesModulosReportes;
import com.coralclubes.facil.modules.reportes.repository.ReportesMotorRepository;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportesMotorService {

    private final ReportesMotorRepository repository;
    private final UserContext userContext;
    private final ObjectMapper mapper;

    // =========================================================================
    // CONSULTAS
    // =========================================================================

    public ApiResponse<List<ReporteDisponibleDto>> obtenerReportesActivos(Integer idRol, ClavesModulosReportes modulo) {
        String claveModulo = modulo != null ? modulo.getClave() : null;
        List<ReporteDisponibleDto> reportes = repository.obtenerReportesActivos(idRol, claveModulo);
        return ApiResponse.success("Reportes activos obtenidos", reportes);
    }

    public ApiResponse<List<ParametroReporteDto>> obtenerParametrosReporte(Integer idTipoReporte) {
        List<ParametroReporteDto> parametros = repository.obtenerParametrosReporte(idTipoReporte);
        return ApiResponse.success("Parámetros del reporte obtenidos", parametros);
    }

    public ApiResponse<List<ParametroMapeoDto>> obtenerParametrosMapeo(Integer idTipoReporte) {
        List<ParametroMapeoDto> mapeo = repository.obtenerParametrosMapeo(idTipoReporte);
        return ApiResponse.success("Mapeo de parámetros obtenido", mapeo);
    }

    public ApiResponse<ProcedimientoEjecucionDto> obtenerProcedimientoEjecucion(Integer idTipoReporte) {
        ProcedimientoEjecucionDto proc = repository.obtenerProcedimientoEjecucion(idTipoReporte);
        if (proc == null) {
            throw new IllegalArgumentException("No se encontró procedimiento de ejecución para el reporte: " + idTipoReporte);
        }
        return ApiResponse.success("Procedimiento obtenido", proc);
    }

    public ApiResponse<List<FavoritoReporteDto>> obtenerFavoritos(Integer idTipoReporte) {
        String usuario = userContext.getUsername();
        List<FavoritoReporteDto> favoritos = repository.obtenerFavoritosUsuario(idTipoReporte, usuario);
        return ApiResponse.success("Favoritos obtenidos", favoritos);
    }

    public ApiResponse<List<ColumnaReporteDto>> obtenerColumnasReporte(Integer idTipoReporte) {
        String usuario = userContext.getUsername();
        ProcedimientoEjecucionDto proc = repository.obtenerProcedimientoEjecucion(idTipoReporte);
        List<ParametroMapeoDto> mapeo = repository.obtenerParametrosMapeo(idTipoReporte);

        if (mapeo.isEmpty()) {
            log.warn("No hay mapeo de parámetros configurado para el reporte {}. Se devolverá metadata del SP si existe.", idTipoReporte);
            if (proc != null) {
                List<ColumnaMetadataDto> metadata = repository.obtenerMetadataColumnas(proc.nombreStoredProcedure(), List.of());
                return ApiResponse.success("Columnas obtenidas de metadata del SP",
                        metadata.stream()
                                .map(m -> new ColumnaReporteDto(m.nombreColumnaDB(), m.ordenOriginal(), true))
                                .toList());
            }
            return ApiResponse.success("Columnas del reporte obtenidas", Collections.emptyList());
        }

        List<ColumnaMetadataDto> metadataSP = repository.obtenerMetadataColumnas(proc.nombreStoredProcedure(), mapeo);
        List<ColumnaReporteDto> preferenciasUsuario = repository.obtenerColumnasUsuario(idTipoReporte, usuario);

        Set<String> columnasPreferencias = preferenciasUsuario.stream()
                .filter(c -> c.visible() != null && c.visible())
                .map(ColumnaReporteDto::nombreColumna)
                .collect(Collectors.toSet());

        List<ColumnaReporteDto> resultado = new java.util.ArrayList<>();
        for (int i = 0; i < metadataSP.size(); i++) {
            ColumnaMetadataDto col = metadataSP.get(i);
            boolean seleccionada = columnasPreferencias.isEmpty() || columnasPreferencias.contains(col.nombreColumnaDB());
            resultado.add(ColumnaReporteDto.builder()
                    .nombreColumna(col.nombreColumnaDB())
                    .orden(i + 1)
                    .visible(seleccionada)
                    .build());
        }

        return ApiResponse.success("Columnas del reporte obtenidas", resultado);
    }

    // =========================================================================
    // EJECUCIÓN CON PERSISTENCIA AUTOMÁTICA
    // =========================================================================

    public ApiResponse<List<Map<String, Object>>> ejecutarReporte(EjecutarReporteRequest request) {
        List<Map<String, Object>> datos = ejecutarYpersistir(request);
        return ApiResponse.success("Reporte ejecutado", datos);
    }

    public List<Map<String, Object>> ejecutarYpersistir(EjecutarReporteRequest request) {
        // 1. Validaciones iniciales
        ProcedimientoEjecucionDto proc = repository.obtenerProcedimientoEjecucion(request.idTipoReporte());
        if (proc == null) {
            throw new IllegalArgumentException("No se encontró el procedimiento para el reporte: " + request.idTipoReporte());
        }

        List<ParametroMapeoDto> mapeo = repository.obtenerParametrosMapeo(request.idTipoReporte());
        if (mapeo.isEmpty()) {
            throw new IllegalArgumentException("No hay mapeo de parámetros configurado para el reporte: " + request.idTipoReporte());
        }

        // 2. Construir parámetros para el SP
        Map<String, Object> parametrosJava = construirParametrosJava(request);
        log.info("Ejecutando reporte {}: SP={}, params={}", request.idTipoReporte(), proc.nombreStoredProcedure(), parametrosJava);

        // 3. Ejecutar el reporte en la BD
        List<Map<String, Object>> datosCrudos = repository.ejecutarReporteMapeado(
                proc.nombreStoredProcedure(),
                mapeo,
                parametrosJava
        );

        // 4. Filtrar columnas solicitadas
        List<Map<String, Object>> datosFiltrados = filtrarColumnas(datosCrudos, request.columnas());

        for (Map<String, Object> datosFiltrado : datosFiltrados) {
            log.debug("Fila resultado: {}", datosFiltrado);
        }

        // 5. Persistir preferencias en BD
        persistirConfiguracion(request.idTipoReporte(), mapeo, parametrosJava, request.columnas());

        return datosFiltrados;
    }

    // =========================================================================
    // UTILIDADES INTERNAS
    // =========================================================================

    private Map<String, Object> construirParametrosJava(EjecutarReporteRequest request) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("FechaInicio", request.fechaInicio());
        params.put("FechaFin", request.fechaFin());
        params.put("UsuarioGenerador", request.usuarioGenerador() != null
                ? request.usuarioGenerador() : userContext.getUsername());
        if (request.parametros() != null) {
            params.putAll(request.parametros());
        }
        return params;
    }

    private List<Map<String, Object>> filtrarColumnas(List<Map<String, Object>> datos, List<String> columnas) {
        if (columnas == null || columnas.isEmpty() || datos.isEmpty()) return datos;

        Set<String> columnasPermitidas = new java.util.HashSet<>(columnas);

        return datos.stream().map(fila -> (Map<String, Object>) fila.entrySet().stream()
                        .filter(e -> columnasPermitidas.contains(e.getKey()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new
                        )))
                .toList();
    }

    // =========================================================================
    // PERSISTENCIA (Favoritos + Columnas)
    // =========================================================================

    private void persistirConfiguracion(Integer idTipoReporte, List<ParametroMapeoDto> mapeo,
                                        Map<String, Object> parametrosJava, List<String> columnasSeleccionadas) {
        String usuario = userContext.getUsername();
        guardarFavoritosAutomatico(idTipoReporte, mapeo, parametrosJava, usuario);
        guardarColumnasAutomatico(idTipoReporte, columnasSeleccionadas, usuario);
    }

    private void guardarFavoritosAutomatico(Integer idTipoReporte, List<ParametroMapeoDto> mapeo,
                                            Map<String, Object> parametrosJava, String usuario) {
        try {
            // Obtenemos los parámetros reales del reporte
            List<ParametroReporteDto> parametrosReporte = repository.obtenerParametrosReporte(idTipoReporte);
            if (parametrosReporte == null || parametrosReporte.isEmpty()) return;

            for (ParametroMapeoDto paramMapeo : mapeo) {
                // Solo nos interesan los catálogos para los favoritos
                if (!"CATALOGO".equals(paramMapeo.rol())) continue;

                Object valorJava = parametrosJava.get(paramMapeo.nombreJava());
                if (valorJava == null) continue;

                // valorCSV será algo como "1,2,3"
                String valorCSV = valorJava.toString().trim();
                if (valorCSV.isBlank()) continue;

                // Buscamos el ID real en la tabla PARAMETROS_REPORTES usando el nombre Java
                Integer idParametroReal = obtenerIdParametroPorNombreJava(parametrosReporte, paramMapeo.nombreJava());
                if (idParametroReal == null) continue;

                // Se manda la cadena CSV completa de un solo golpe sin hacer FOR ni SPLIT
                repository.guardarFavoritosUsuario(idTipoReporte, usuario, idParametroReal, valorCSV, 1);
            }
        } catch (Exception e) {
            log.warn("Error guardando favoritos para reporte {}: {}", idTipoReporte, e.getMessage());
        }
    }

    private void guardarColumnasAutomatico(Integer idTipoReporte, List<String> columnasSeleccionadas, String usuario) {
        if (columnasSeleccionadas == null || columnasSeleccionadas.isEmpty()) return;

        try {
            List<Map<String, Object>> listaJson = new java.util.ArrayList<>();
            for (int i = 0; i < columnasSeleccionadas.size(); i++) {
                listaJson.add(Map.of(
                        "nombreColumna", columnasSeleccionadas.get(i),
                        "orden", i + 1
                ));
            }
            String jsonStr = mapper.writeValueAsString(listaJson);
            repository.guardarColumnasUsuarioMasivo(idTipoReporte, usuario, jsonStr);
        } catch (Exception e) {
            log.warn("Error guardando columnas automáticas para reporte {}: {}", idTipoReporte, e.getMessage());
        }
    }

    private Integer obtenerIdParametroPorNombreJava(List<ParametroReporteDto> parametrosReporte, String nombreJava) {
        return parametrosReporte.stream()
                .filter(p -> p.nombreFiltroUI().equalsIgnoreCase(nombreJava) ||
                        (p.endpointData() != null && p.endpointData().toUpperCase().contains(nombreJava.toUpperCase())))
                .map(ParametroReporteDto::idParametro)
                .findFirst()
                .orElse(null);
    }

    // =========================================================================
    // HISTORIAL Y GENERACIÓN ASÍNCRONA (NUEVO FLUJO)
    // =========================================================================

    /**
     * Obtiene el historial de reportes generados por el usuario actual (Últimos 50)
     */
    public ApiResponse<List<HistorialReporteDto>> obtenerHistorialUsuario(
            ClavesModulosReportes clave
    ) {
        String usuario = userContext.getUsername();
        List<HistorialReporteDto> historial = repository.obtenerHistorialUsuario(usuario, 50, clave.getClave());
        return ApiResponse.success("Historial de reportes obtenido", historial);
    }

    /**
     * Solo registra la solicitud en la bitácora y devuelve el ID generado.
     */
    public Integer registrarInicioReporteAsync(EjecutarReporteRequest request, String nombreReporteBase) {
        try {
            String parametrosJson = mapper.writeValueAsString(request.parametros());
            String nombreTemporal = nombreReporteBase + "_Procesando.xlsx";
            String usuario = userContext.getUsername();

            Integer idBitacora = repository.registrarInicioReporte(usuario, request.idTipoReporte(), nombreTemporal, parametrosJson);

            if (idBitacora == null) {
                throw new IllegalStateException("No se pudo obtener el ID de la bitácora.");
            }

            // guardamos las preferencias del usario para este reporte
            persistirConfiguracion(request.idTipoReporte(), repository.obtenerParametrosMapeo(request.idTipoReporte()), request.parametros(), request.columnas());

            return idBitacora;
        } catch (Exception e) {
            log.error("Error al registrar inicio de bitácora: {}", e.getMessage());
            throw new RuntimeException("No se pudo registrar el reporte en el historial.");
        }
    }
}