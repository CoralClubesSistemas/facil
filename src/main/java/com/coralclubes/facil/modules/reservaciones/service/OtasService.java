package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.request.AgregarOtaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.ConfiguracionUnidadesRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.CrearConfiguracionOtaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.request.GenerarReservacionOtaRequest;
import com.coralclubes.facil.modules.reservaciones.dto.response.ConfiguracionOtaResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.GenerarReservacionOtaResponse;
import com.coralclubes.facil.modules.reservaciones.dto.response.UnidadOtaResponse;
import com.coralclubes.facil.modules.reservaciones.repository.OtasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OtasService {

    private final OtasRepository otasRepository;

    public void agregarOta(AgregarOtaRequest request) {
        otasRepository.spResvAgregarNuevaOta(request.nombreOta());
    }

    public Integer crearConfiguracionOta(CrearConfiguracionOtaRequest request, String usuario) {
        return otasRepository.spResvCrearConfiguracionOta(
                request.idOta(),
                request.idDesarrollo(),
                request.fechaInicio(),
                request.fechaFin(),
                request.porcentajeComision(),
                usuario
        ).orElseThrow(() -> new RuntimeException("Error al crear la configuración de la OTA."));
    }

    public Integer agregarUnidadesConfiguracionOta(Integer idConfiguracionOta, ConfiguracionUnidadesRequest request, String usuario) {
        return otasRepository.spResvAgregarUnidadesConfiguracionOta(idConfiguracionOta, request.unidades(), usuario)
                .orElseThrow(() -> new RuntimeException("Error al agregar unidades a la configuración de la OTA."));
    }

    public Integer eliminarUnidadesConfiguracionOta(Integer idConfiguracionOta, ConfiguracionUnidadesRequest request, String usuario) {
        return otasRepository.spResvEliminarUnidadesConfiguracionOta(idConfiguracionOta, request.unidades(), usuario)
                .orElseThrow(() -> new RuntimeException("Error al eliminar unidades de la configuración de la OTA."));
    }

    public Integer desactivarConfiguracionOta(Integer idConfiguracionOta, String usuario) {
        return otasRepository.spResvDesactivarConfiguracionOta(idConfiguracionOta, usuario)
                .orElseThrow(() -> new RuntimeException("Error al desactivar la configuración de la OTA."));
    }

    public List<ConfiguracionOtaResponse> obtenerConfiguracionesOtas() {
        return otasRepository.spResvObtenerConfiguracionesOtas();
    }

    public List<UnidadOtaResponse> obtenerUnidadesConfiguracionOta(Integer idConfiguracionOta) {
        return otasRepository.spResvObtenerUnidadesConfiguracionOta(idConfiguracionOta);
    }

    public List<UnidadOtaResponse> obtenerUnidadesDisponiblesParaOta(
            Integer idConfiguracionOta,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        return otasRepository.spResvObtenerUnidadesDisponiblesParaOta(idConfiguracionOta, fechaInicio, fechaFin);
    }

    public List<UnidadOtaResponse> buscarDisponibilidadUnidadesOta(
            Integer idDesarrollo,
            Integer tipoUnidad,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        return otasRepository.spResvBuscarDisponibilidadUnidadesOta(idDesarrollo, tipoUnidad, fechaInicio, fechaFin);
    }

    public GenerarReservacionOtaResponse generarReservacionOta(GenerarReservacionOtaRequest request, String usuario) {
        return otasRepository.spResvGeneraReservacionOta(
                request.idOta(),
                request.idDesarrollo(),
                request.codigoVoucherOta(),
                request.montoTarifaOta(),
                request.rsvMembresia(),
                request.nombreReservacion(),
                request.correoElectronico(),
                request.telefono(),
                request.fechaInicio(),
                request.fechaFin(),
                request.tipoUnidad(),
                request.idUnidad(),
                request.numeroSocios() != null ? request.numeroSocios() : 0,
                request.peticionEspecial(),
                usuario
        ).orElseThrow(() -> new RuntimeException("Error al generar la reservación desde OTA."));
    }
}
