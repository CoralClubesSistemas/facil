package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.service.CobranzaCatalogosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cobranza/catalogos")
@RequiredArgsConstructor
public class CobranzaCatalogosController {
    private final CobranzaCatalogosService service;

    // =========================================================================
    // CATÁLOGOS COMERCIALES (admin, equivalente a public)
    // =========================================================================

    @GetMapping("/tipos-series")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTiposSeries() {
        return ResponseEntity.ok(service.obtenerCatalogoTiposSeries());
    }

    @GetMapping("/porcentaje-autorizado")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerPorcentajeAutorizado(
            @RequestParam("idDesarrollo") Integer idDesarrollo,
            @RequestParam("clasificacionMembresia") Integer clasificacionMembresia) {
        return ResponseEntity.ok(service.obtenerPorcentajeAutorizado(idDesarrollo, clasificacionMembresia));
    }

    @GetMapping("/terminales")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerTerminales() {
        return ResponseEntity.ok(service.obtenerCatalogoTerminales());
    }

    @GetMapping("/bancos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoBancos() {
        return ResponseEntity.ok(service.obtenerCatalogoBancos());
    }

    @GetMapping("/tipos-movimientos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoTiposMovimientos() {
        return ResponseEntity.ok(service.obtenerCatalogoTiposMovimientos());
    }

    @GetMapping("/estatus-movimientos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoEstatusMovimientos() {
        return ResponseEntity.ok(service.obtenerCatalogoEstatusMovimientos());
    }

    @GetMapping("/desarrollos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoDesarrollos() {
        return ResponseEntity.ok(service.obtenerCatalogoDesarrollos());
    }

    @GetMapping("/tipos-membresias")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoTiposMembresias(
            @RequestParam(required = false) Integer idClasificacion,
            @RequestParam(required = false) Integer idDesarrollo
    ) {
        return ResponseEntity.ok(service.obtenerCatalogoTiposMembresias(idClasificacion, idDesarrollo));
    }

    @GetMapping("/estatus-puntos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoEstatusPuntos() {
        return ResponseEntity.ok(service.obtenerCatalogoEstatusPuntos());
    }

    @GetMapping("/clave/{clave}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoPorClave(
            @PathVariable String clave
    ) {
        return ResponseEntity.ok(service.obtenerCatalogoPorClave(clave));
    }
}
