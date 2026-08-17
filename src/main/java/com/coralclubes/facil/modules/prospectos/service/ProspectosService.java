package com.coralclubes.facil.modules.prospectos.service;

import com.coralclubes.facil.modules.prospectos.dto.request.ProspectoCrearRequest;
import com.coralclubes.facil.modules.prospectos.dto.request.ProspectoRegistrarCitaRequest;
import com.coralclubes.facil.modules.prospectos.repository.ProspectosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Capa de servicio para la gestión de prospectos de venta y citas.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProspectosService {

    private final ProspectosRepository prospectosRepository;

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
}
