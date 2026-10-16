package com.coralclubes.facil.modules.dashboard.controller;

import com.coralclubes.facil.modules.dashboard.service.DashHospedajeService;
import com.coralclubes.facil.modules.dashboard.dto.response.HospedajeDetalleMovimientoResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.HospedajeLibreResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.HospedajesDisponiblesResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.ReservasCanceladasResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.SinReservaDetalleMovimientoResponse;
import com.coralclubes.facil.modules.dashboard.dto.response.TotalCategoriaResponse;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/hospedaje")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashHospedajeController {

    private final DashHospedajeService dashHospedajeService;

    @GetMapping("/hospedaje-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> obtenerHospedajeTotales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.obtenerHospedajeTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/hospedaje-totales-estatus")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerHospedajeTotalesEstatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.obtenerHospedajeTotalesEstatus(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/hospedajes-desarrollos")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> obtenerHospedajesDesarrollos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.obtenerHospedajesDesarrollos(fechaInicio, fechaFin)));
    }

    @GetMapping("/hospedajes-libres")
    public ResponseEntity<ApiResponse<List<HospedajeLibreResponse>>> obtenerHospedajesLibres(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.obtenerHospedajesLibres(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/disponibilidadx-mes")
    public ResponseEntity<ApiResponse<List<HospedajesDisponiblesResponse>>> obtenerDisponibilidadxMes(
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) String anio,
            @RequestParam(required = false) String desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.obtenerDisponibilidadxMes(mes, anio, desarrollo)));
    }

    @GetMapping("/hospedajes-detalles-movimientos")
    public ResponseEntity<ApiResponse<List<HospedajeDetalleMovimientoResponse>>> obtenerHospedajesDetallesMovimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.obtenerHospedajesDetallesMovimientos(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/reservas-canceladas")
    public ResponseEntity<ApiResponse<List<ReservasCanceladasResponse>>> obtenerReservasCanceladas(
            @RequestParam(required = false) String idDesarrollo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.obtenerReservasCanceladas(idDesarrollo, fechaEntrada, fechaSalida)));
    }

    @GetMapping("/sin-reserva-detalles-movimientos")
    public ResponseEntity<ApiResponse<List<SinReservaDetalleMovimientoResponse>>> obtenerSinReservaDetallesMovimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer desarrollo
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.obtenerSinReservaDetallesMovimientos(fechaInicio, fechaFin, desarrollo)));
    }
}
