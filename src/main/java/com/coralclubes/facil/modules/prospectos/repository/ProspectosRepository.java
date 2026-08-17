package com.coralclubes.facil.modules.prospectos.repository;

import com.coralclubes.facil.modules.prospectos.dto.request.ProspectoCrearRequest;
import com.coralclubes.facil.modules.prospectos.dto.request.ProspectoRegistrarCitaRequest;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio del módulo de Prospectos.
 * Invoca exclusivamente Stored Procedures mediante StoredProcedureExecutor.
 */
@Repository
@RequiredArgsConstructor
public class ProspectosRepository {

    private final StoredProcedureExecutor executor;

    /**
     * Ejecuta el Stored Procedure spProspectoCrearProspecto para insertar o actualizar un prospecto de venta.
     * @return ID del prospecto creado o actualizado.
     */
    public Integer spProspectoCrearProspecto(ProspectoCrearRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("id_externo", request.idExterno());
        params.put("origen", request.origen());
        params.put("nombre", request.nombre());
        params.put("segundo_nombre", request.segundoNombre());
        params.put("apellido_paterno", request.apellidoPaterno());
        params.put("apellido_materno", request.apellidoMaterno());
        params.put("email", request.email());
        params.put("telefono", request.telefono());
        params.put("cargo", request.cargo());
        params.put("edad", request.edad());
        params.put("desarrollo_interes", request.desarrolloInteres());
        params.put("lsv_area_interes", request.lsvAreaInteres());
        params.put("data_adicional", request.dataAdicional());
        params.put("estatus", request.estatus());

        return executor.querySingle("spProspectoCrearProspecto", params, (rs, rowNum) -> rs.getInt(1))
                .orElse(null);
    }

    /**
     * Ejecuta el Stored Procedure spProspectoRegistarCita para registrar una cita a un prospecto.
     */
    public void spProspectoRegistarCita(ProspectoRegistrarCitaRequest request, String usuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("prospecto_id", request.prospectoId());
        params.put("fecha_inicio", request.fechaInicio());
        params.put("hora_inicio", request.horaInicio());
        params.put("lugar_cita", request.lugarCita());
        params.put("nota", request.nota());
        params.put("usuario", usuario);

        executor.execute("spProspectoRegistarCita", params);
    }
}
