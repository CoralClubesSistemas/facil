package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reservaciones.dto.projection.CatalogoCargosHabitacion;
import com.coralclubes.facil.modules.reservaciones.dto.response.CaracteristicaDto;
import com.coralclubes.facil.modules.reservaciones.repository.CatalogosRepository;
import com.coralclubes.facil.shared.infrastructure.domain.dto.CatalogoListasValores;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogosService {

    private final CatalogosRepository repo;
    private final UserContext userContext;

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerHoteles() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        return ApiResponse.success(repo.spResvCatalogoHoteles(idDesarrollo));
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerTiposHabitaciones() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        return ApiResponse.success(repo.spResvCatalogoTiposHabitaciones(idDesarrollo));
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerDestinos() {
        Integer idDesarrollo = userContext.getIdDesarrollo();

        return ApiResponse.success(repo.spResvCatalogoDestinos(idDesarrollo));
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerTemporadas() {
        return ApiResponse.success(repo.spResvCatalogoTemporadas());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerTiposAccesos() {
        return ApiResponse.success(repo.spResvCatalogoTiposAccesos());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerTiposTarifas() {
        return ApiResponse.success(repo.spResvCatalogoTiposTarifas());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerOrigenesReservas() {
        return ApiResponse.success(repo.spResvCatalogoOrigenesReservas());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerPeriodoTarifa() {
        return ApiResponse.success(repo.spResvCatalogoPeriodoTarifa());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerTiposUnidades(String idHoteles) {
        // si el hotel es null se carga el del usuario
        Integer idDesarrollo = userContext.getIdDesarrollo();

        return ApiResponse.success(repo.spResvCatalogoTiposUnidades(idHoteles != null ? idHoteles : idDesarrollo.toString()));
    }

    public ApiResponse<List<SelectGenerico<String>>> obtenerTiposPromociones() {
        return ApiResponse.success(repo.spResvCatalogoTiposPromociones());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerTiposOferta() {
        return ApiResponse.success(repo.spResvCatalogoTiposOferta());
    }

    public ApiResponse<List<SelectGenerico<String>>> obtenerAccionesObjetivo() {
        return ApiResponse.success(repo.spResvCatalogoAccionObjetivo());
    }

    public ApiResponse<List<CaracteristicaDto>> obtenerCaracteristicas() {
        return ApiResponse.success(repo.spResvObtenerCaracteristicasReservaciones());
    }

    public ApiResponse<List<SelectGenerico<String>>> obtenerTiposReglas() {
        return ApiResponse.success(repo.spResvCatalogoTiposReglas());
    }

    public ApiResponse<List<SelectGenerico<String>>> obtenerComparadores() {
        return ApiResponse.success(repo.spResvCatalogoComparadores());
    }

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCargosHabitacion(String membresia) {
        return ApiResponse.success(repo.spResvCatalogoCargosHabitacion(membresia));
    }
}