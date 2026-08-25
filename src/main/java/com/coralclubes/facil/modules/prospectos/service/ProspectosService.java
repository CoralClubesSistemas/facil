package com.coralclubes.facil.modules.prospectos.service;

import com.coralclubes.facil.modules.prospectos.dto.domain.EventoResultadoCita;
import com.coralclubes.facil.modules.prospectos.dto.request.ProspectoCrearRequest;
import com.coralclubes.facil.modules.prospectos.dto.request.ProspectoRegistrarCitaRequest;
import com.coralclubes.facil.modules.prospectos.dto.request.RegistrarResultadoCitaRequest;
import com.coralclubes.facil.modules.prospectos.repository.ProspectosRepository;
import com.coralclubes.facil.shared.infrastructure.integration.crm.service.CrmEventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Capa de servicio para la gestión de prospectos de venta, citas y emisión agnóstica de eventos de resultado.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProspectosService {

    private final ProspectosRepository prospectosRepository;
    private final CrmEventPublisherService crmEventPublisherPort;

    /**
     * Crea o actualiza la información de un prospecto de venta.
     *
     * @param request Datos del prospecto a registrar/actualizar.
     * @return ID del prospecto creado o actualizado.
     */
    public Integer crearOActualizarProspecto(ProspectoCrearRequest request) {
        log.info("[PROSPECTOS SERVICE] Registrando/actualizando prospecto con ID externo: {}, email: {}",
                request.idExterno(), request.email());

        Integer prospectoId = prospectosRepository.spProspectoCrearProspecto(request);

        log.info("[PROSPECTOS SERVICE] Prospecto con ID externo {} procesado exitosamente con ID interno: {}",
                request.idExterno(), prospectoId);

        return prospectoId;
    }

    /**
     * Registra una cita para un prospecto.
     *
     * @param request Datos de la cita a agendar.
     * @param usuario Usuario que ejecuta el registro de la cita.
     */
    public void registrarCitaProspecto(ProspectoRegistrarCitaRequest request, String usuario) {
        log.info("[PROSPECTOS SERVICE] Registrando cita para el prospecto ID: {} en fecha: {} {}, usuario: {}",
                request.prospectoId(), request.fechaInicio(), request.horaInicio(), usuario);

        prospectosRepository.spProspectoRegistarCita(request, usuario);
    }

    /**
     * Procesa el resultado de la cita de un prospecto:
     * 1. Ejecuta spProspectoActualizarEstatusCita si se proporcionó idCita para actualizar la base de datos local.
     * 2. Emite el evento de negocio hacia el CRM configurado.
     *
     * @param request Información del resultado de la cita (asistió/compró/no asistió y datos de compra).
     * @param usuario Usuario que registra el resultado.
     * @return true si el proceso se ejecutó exitosamente.
     */
    public boolean procesarResultadoCita(RegistrarResultadoCitaRequest request, String usuario) {
        log.info("[PROSPECTOS SERVICE] Procesando resultado de cita '{}' para Lead ID Externo: {}, Cita ID: {}, Usuario: {}",
                request.resultado(), request.idExterno(), request.idCita(), usuario);

        // Paso 1: Actualizar estatus de la cita en BD mediante SP si idCita está presente
        if (request.idCita() != null) {
            log.info("[PROSPECTOS SERVICE] Ejecutando spProspectoActualizarEstatusCita para Cita ID: {}", request.idCita());
            prospectosRepository.spProspectoActualizarEstatusCita(
                    request.idCita(),
                    request.resultado().name(),
                    request.observaciones(),
                    request.datosCompra() != null ? request.datosCompra().membresia() : null
            );
            log.info("[PROSPECTOS SERVICE] Cita ID: {} actualizada exitosamente en BD", request.idCita());
        } else {
            log.warn("[PROSPECTOS SERVICE] idCita no fue proporcionado en el request. Omitiendo ejecución de spProspectoActualizarEstatusCita.");
        }

        // Paso 2: Emitir evento hacia el CRM configurado
        EventoResultadoCita evento = EventoResultadoCita.builder()
                .idExterno(request.idExterno())
                .idCita(request.idCita())
                .resultado(request.resultado())
                .desarrollo(request.desarrollo())
                .observaciones(request.observaciones())
                .datosCompra(request.datosCompra())
                .usuario(usuario)
                .build();

        crmEventPublisherPort.publicarResultadoCita(evento);

        log.info("[PROSPECTOS SERVICE] Evento de resultado de cita despachado hacia el puerto CRM exitosamente para Lead: {}",
                request.idExterno());

        return true;
    }
}
