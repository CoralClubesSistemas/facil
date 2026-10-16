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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard/hospedaje")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MOD_MNUDASHBOARD')")
public class DashHospedajeController {

    private final DashHospedajeService dashHospedajeService;

    @GetMapping("/hospedaje-totales")
    public ResponseEntity<ApiResponse<TotalCategoriaResponse<Long>>> spDashHospedajeTotales(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.spDashHospedajeTotales(fechaInicio, fechaFin).orElse(null)));
    }

    @GetMapping("/hospedaje-totales-estatus")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashHospedajeTotalesEstatus(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.spDashHospedajeTotalesEstatus(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/hospedajes-desarrollos")
    public ResponseEntity<ApiResponse<List<TotalCategoriaResponse<Long>>>> spDashHospedajesDesarrollos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.spDashHospedajesDesarrollos(fechaInicio, fechaFin)));
    }

    @GetMapping("/hospedajes-libres")
    public ResponseEntity<ApiResponse<List<HospedajeLibreResponse>>> spDashHospedajesLibres(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.spDashHospedajesLibres(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/disponibilidadx-mes")
    public ResponseEntity<ApiResponse<List<HospedajesDisponiblesResponse>>> spDashDisponibilidadxMes(@RequestParam(required = false) String mes, @RequestParam(required = false) String anio, @RequestParam(required = false) String desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.spDashDisponibilidadxMes(mes, anio, desarrollo)));
    }

    @GetMapping("/hospedajes-detalles-movimientos")
    public ResponseEntity<ApiResponse<List<HospedajeDetalleMovimientoResponse>>> spDashHospedajesDetallesMovimientos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.spDashHospedajesDetallesMovimientos(fechaInicio, fechaFin, desarrollo)));
    }

    @GetMapping("/reservas-canceladas")
    public ResponseEntity<ApiResponse<List<ReservasCanceladasResponse>>> spDashReservasCanceladas(@RequestParam(required = false) String idDesarrollo, @RequestParam(required = false) String fechaEntrada, @RequestParam(required = false) String fechaSalida) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.spDashReservasCanceladas(idDesarrollo, fechaEntrada, fechaSalida)));
    }

    @GetMapping("/sin-reserva-detalles-movimientos")
    public ResponseEntity<ApiResponse<List<SinReservaDetalleMovimientoResponse>>> spDashSinReservaDetallesMovimientos(@RequestParam(required = false) String fechaInicio, @RequestParam(required = false) String fechaFin, @RequestParam(required = false) Integer desarrollo) {
        return ResponseEntity.ok(ApiResponse.success(dashHospedajeService.spDashSinReservaDetallesMovimientos(fechaInicio, fechaFin, desarrollo)));
    }
}
