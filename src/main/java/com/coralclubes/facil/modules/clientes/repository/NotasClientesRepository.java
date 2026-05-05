package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.clientes.dto.projection.ArchivoNotaProjection;
import com.coralclubes.facil.modules.clientes.dto.response.CrearNotaUsuarioResponse;
import com.coralclubes.facil.modules.clientes.dto.response.NotasClienteResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class NotasClientesRepository {
    private final StoredProcedureExecutor executor;

    private final RowMapper<NotasClienteResponse> notasClienteMapper = (rs, rowNum) ->
            new NotasClienteResponse(
                    rs.getString("campoAlarma"),
                    rs.getString("membresia"),
                    rs.getObject("consecutivo", Integer.class),
                    rs.getTimestamp("fechaNota") != null ? rs.getTimestamp("fechaNota").toLocalDateTime() : null,
                    rs.getObject("clasificacionNotaId", Integer.class),
                    rs.getString("clasificacion"),
                    rs.getString("usuarioRegistra"),
                    rs.getString("nota"),
                    rs.getTimestamp("fechaFinAlerta") != null ? rs.getTimestamp("fechaFinAlerta").toLocalDateTime() : null,
                    rs.getString("usuarioDesactivaAlerta"),
                    rs.getString("respondio"),
                    rs.getString("telefono"),
                    rs.getString("extension"),
                    rs.getString("tipoTelefono")
            );

    private final RowMapper<SelectGenerico<Integer>> selectGenericoMapper = (rs, rowNum) ->
            new SelectGenerico<>(rs.getInt(1), rs.getString(2));

    private final RowMapper<ArchivoNotaProjection> archivoNotaMapper = (rs, rowNum) ->
            new ArchivoNotaProjection(
                    rs.getString("nombreArchivo"),
                    UUID.fromString(rs.getString("uuidArchivo")),
                    rs.getString("tipoArchivo"),
                    rs.getString("usuarioCarga"),
                    rs.getTimestamp("fechaCarga") != null ? rs.getTimestamp("fechaCarga").toLocalDateTime() : null
            );

    private final RowMapper<CrearNotaUsuarioResponse> crearNotaUsuarioMapper = (rs, rowNum) ->
            new CrearNotaUsuarioResponse(
                    rs.getString("membresia"),
                    rs.getObject("consecutivo", Integer.class)
            );

    public List<NotasClienteResponse> spBuscarNotasCliente(
            String numeroMembresia,
            LocalDateTime fechaRangoInicial,
            LocalDateTime fechaRangoFinal,
            Integer clasificaNota
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("NumeroMembresia", numeroMembresia);
        params.put("FechaRangoInicial", fechaRangoInicial);
        params.put("FechaRangoFinal", fechaRangoFinal);
        params.put("ClasificaNota", clasificaNota);

        return executor.queryList("spBuscarNotasCliente", params, notasClienteMapper);
    }

    public List<SelectGenerico<Integer>> spObtenerClasificacionNotasXUsuario(Integer rolId) {
        return executor.queryList("spObtenerClasificacionNotasXUsuario", Map.of(
                "Rol", rolId
        ), selectGenericoMapper);
    }

    public Optional<CrearNotaUsuarioResponse> spCrearNotaUsuario(String membresia, String usuario, Integer clasificacionNota, String nota, Boolean alerta) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Usuario", usuario);
        params.put("ClasificacionNota", clasificacionNota);
        params.put("Nota", nota);
        params.put("Alerta", alerta);

        return executor.querySingle("spCrearNotaUsuario", params, crearNotaUsuarioMapper);
    }

    public void spRegistrarArhivosNotas(String membresia, Integer consecutivo, String nombreArchivo, String uuidArchivo, String tipoArchivo, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Consecutivo", consecutivo);
        params.put("NombreArchivo", nombreArchivo);
        params.put("UuidArchivo", uuidArchivo);
        params.put("TipoArchivo", tipoArchivo);
        params.put("Usuario", usuario);

        executor.execute("spRegistrarArhivosNotas", params);
    }

    public List<ArchivoNotaProjection> spObtenerArchivosNotas(String membresia, Integer consecutivo) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "Consecutivo", consecutivo
        );

        return executor.queryList("spObtenerArchivosNotas", params, archivoNotaMapper);
    }
}

