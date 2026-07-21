package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.MembresiaLlamadaResponse;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaTelefonoResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TelefonosRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<MembresiaTelefonoResponse> telefonoRowMapper = (rs, rowNum) ->
            MembresiaTelefonoResponse.builder()
                    .membresia(rs.getString("membresia"))
                    .numeroTelefono(rs.getString("numero_telefono"))
                    .lada(rs.getString("lada"))
                    .extensionPrincipal(rs.getString("extension_principal"))
                    .extensionAlterna(rs.getString("extension_alterna"))
                    .tipoTelefono(rs.getString("tipo_telefono"))
                    .estatusTelefono(rs.getString("estatus_telefono"))
                    .prioridadTelefono(rs.getString("prioridad_telefono"))
                    .observaciones(rs.getString("observaciones"))
                    .usuarioRegistra(rs.getString("usuario_registra"))
                    .fechaRegistro(rs.getTimestamp("fecha_registro") != null ? rs.getTimestamp("fecha_registro").toLocalDateTime() : null)
                    .fechaUltimaActualizacion(rs.getTimestamp("fecha_ultima_actualizacion") != null ? rs.getTimestamp("fecha_ultima_actualizacion").toLocalDateTime() : null)
                    .usuarioUltimaActualizacion(rs.getString("usuario_ultima_actualizacion"))
                    .build();

    private final RowMapper<MembresiaLlamadaResponse> llamadaRowMapper = (rs, rowNum) ->
            MembresiaLlamadaResponse.builder()
                    .id(rs.getObject("id", Integer.class))
                    .membresia(rs.getString("membresia"))
                    .fechaRegistro(rs.getTimestamp("fecha_registro") != null ? rs.getTimestamp("fecha_registro").toLocalDateTime() : null)
                    .usuario(rs.getString("usuario"))
                    .extension(rs.getString("extension"))
                    .telefono(rs.getString("telefono"))
                    .fechaInicio(rs.getTimestamp("fecha_inicio") != null ? rs.getTimestamp("fecha_inicio").toLocalDateTime() : null)
                    .fechaFin(rs.getTimestamp("fecha_fin") != null ? rs.getTimestamp("fecha_fin").toLocalDateTime() : null)
                    .duracion(rs.getObject("duracion", Integer.class))
                    .build();

    public List<MembresiaTelefonoResponse> spMembresiaObtenerNumerosTelefonos(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);
        return spExecutor.queryList("spMembresiaObtenerNumerosTelefonos", params, telefonoRowMapper);
    }

    public void spMembresiaActualizarEstatusTelefono(String membresia, String numeroTelefono, Boolean estatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("NumeroTelefono", numeroTelefono);
        params.put("Estatus", estatus);
        spExecutor.execute("spMembresiaActualizarEstatusTelefono", params);
    }

    public void spMembresiaActualizarDatosTelefono(
            String membresia,
            String numeroTelefono,
            String nuevoNumeroTelefono,
            String lada,
            String extensionPrincipal,
            String extensionAlterna,
            Integer tipoTelefono,
            String observaciones,
            String usuario
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("NumeroTelefono", numeroTelefono);
        params.put("NuevoNumeroTelefono", nuevoNumeroTelefono);
        params.put("Lada", lada);
        params.put("ExtensionPrincipal", extensionPrincipal);
        params.put("ExtensionAlterna", extensionAlterna);
        params.put("TipoTelefono", tipoTelefono);
        params.put("Observaciones", observaciones);
        params.put("Usuario", usuario);
        spExecutor.execute("spMembresiaActualizarDatosTelefono", params);
    }

    public void spMembresiaReordenarPrioridadTelefonos(String membresia, String reordenamiento, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Reordenamiento", reordenamiento);
        params.put("Usuario", usuario);
        spExecutor.execute("spMembresiaReordenarPrioridadTelefonos", params);
    }

    public List<MembresiaLlamadaResponse> spMembresiaObtenerBitacoraLlamadas(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);
        return spExecutor.queryList("spMembresiaObtenerBitacoraLlamadas", params, llamadaRowMapper);
    }
}
