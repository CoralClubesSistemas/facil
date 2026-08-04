package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.dto.request.DuplicarCuponesMasivoRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarFormatoImagenCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponDetalleResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponImagenFormatoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponListadoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCanjesPorConceptoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCatalogoElementoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesEstadisticasKpiResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesTopCanjeadosResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesUsoMensualResponse;
import com.coralclubes.facil.modules.cobranza.service.CuponesService;
import com.coralclubes.facil.modules.usuarios.service.UserContext;
import com.coralclubes.facil.shared.domain.dto.ArchivoDescarga;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cobranza/cupones")
@RequiredArgsConstructor
public class CuponesAdminController {

    private final CuponesService cuponesService;
    private final UserContext userContext;

    @GetMapping("/catalogos/condiciones")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponesCatalogoElementoResponse>>> obtenerCatalogoCondiciones() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de condiciones obtenido exitosamente", cuponesService.obtenerCatalogoCondiciones()));
    }

    @GetMapping("/catalogos/beneficios")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponesCatalogoElementoResponse>>> obtenerCatalogoBeneficios() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de beneficios obtenido exitosamente", cuponesService.obtenerCatalogoBeneficios()));
    }

    @GetMapping("/catalogos/origenes")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<String>>>> obtenerCatalogoOrigenes() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de orígenes obtenido exitosamente", cuponesService.obtenerCatalogoOrigenes()));
    }

    @GetMapping("/catalogos/conceptos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoConceptos() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de conceptos obtenido exitosamente", cuponesService.obtenerCatalogoConceptos()));
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Integer>> guardarCupon(@Valid @RequestBody GuardarCuponRequest request) {
        String usuario = userContext.getUsername();

        Integer idCupon = cuponesService.guardarCupon(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Cupón guardado correctamente", idCupon));
    }

    @GetMapping("/listado")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponListadoResponse>>> obtenerListadoCupones(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer desarrollo,
            @RequestParam(required = false) String origen
    ) {
        List<CuponListadoResponse> listado = cuponesService.obtenerListadoCupones(year, desarrollo, origen);
        return ResponseEntity.ok(ApiResponse.success("Listado de cupones obtenido exitosamente", listado));
    }

    @PostMapping("/imagen/upload-url")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<RespuestaCargaDto>> solicitarUrlCargaImagenCupon(
            @Valid @RequestBody SolicitarUrlRequest request) {
        String usuario = userContext.getUsername();

        RespuestaCargaDto respuesta = cuponesService.solicitarUrlCarga(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("URL de carga de imagen solicitada correctamente", respuesta));
    }

    @PostMapping("/imagen")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> registrarImagenCupon(
            @Valid @RequestBody GuardarFormatoImagenCuponRequest request) {
        String usuario = userContext.getUsername();
        cuponesService.guardarImagenCupon(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Formato de imagen de cupón guardado correctamente", null));
    }

    @GetMapping("/{idCupon}/formato-imagen")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<CuponImagenFormatoResponse>> obtenerImagenCupon(
            @PathVariable Integer idCupon) {
        CuponImagenFormatoResponse response = cuponesService.obtenerImagenCupon(idCupon);
        return ResponseEntity.ok(ApiResponse.success("Formato de imagen obtenido exitosamente", response));
    }

    @DeleteMapping("/{idCupon}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> eliminarCupon(@PathVariable Integer idCupon) {
        String usuario = userContext.getUsername();
        cuponesService.eliminarCupon(idCupon, usuario);
        return ResponseEntity.ok(ApiResponse.success("Cupón eliminado correctamente", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<CuponDetalleResponse>> obtenerDetalleCupon(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Detalle obtenido exitosamente", cuponesService.obtenerDetalleCupon(id)));
    }

    @PostMapping("/{idCupon}/reactivar")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> reactivarCupon(@PathVariable Integer idCupon) {
        String usuario = userContext.getUsername();
        cuponesService.reactivarCupon(idCupon, usuario);
        return ResponseEntity.ok(ApiResponse.success("Cupón reactivado correctamente", null));
    }

    @GetMapping("/desactivados")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponListadoResponse>>> obtenerCuponesDesactivados(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer desarrollo
    ) {
        List<CuponListadoResponse> listado = cuponesService.obtenerCuponesDesactivados(year, desarrollo);
        return ResponseEntity.ok(ApiResponse.success("Listado de cupones desactivados obtenido exitosamente", listado));
    }

    @GetMapping("/estadisticas/kpis")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<CuponesEstadisticasKpiResponse>> obtenerEstadisticasKpis(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer desarrollo
    ) {
        CuponesEstadisticasKpiResponse kpis = cuponesService.obtenerEstadisticasKpis(anio, desarrollo);
        return ResponseEntity.ok(ApiResponse.success("KPIs de cupones obtenidos exitosamente", kpis));
    }

    @GetMapping("/estadisticas/uso-mensual")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponesUsoMensualResponse>>> obtenerEstadisticasUsoMensual(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer desarrollo
    ) {
        List<CuponesUsoMensualResponse> usoMensual = cuponesService.obtenerEstadisticasUsoMensual(anio, desarrollo);
        return ResponseEntity.ok(ApiResponse.success("Uso mensual de cupones obtenido exitosamente", usoMensual));
    }

    @GetMapping("/estadisticas/canjes-concepto")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponesCanjesPorConceptoResponse>>> obtenerEstadisticasCanjesPorConcepto(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer desarrollo
    ) {
        List<CuponesCanjesPorConceptoResponse> canjesPorConcepto = cuponesService.obtenerEstadisticasCanjesPorConcepto(anio, desarrollo);
        return ResponseEntity.ok(ApiResponse.success("Canjes por concepto obtenidos exitosamente", canjesPorConcepto));
    }

    @GetMapping("/estadisticas/top-canjeados")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<CuponesTopCanjeadosResponse>>> obtenerEstadisticasTopCanjeados(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer desarrollo,
            @RequestParam(required = false, defaultValue = "5") Integer top
    ) {
        List<CuponesTopCanjeadosResponse> topCanjeados = cuponesService.obtenerEstadisticasTopCanjeados(anio, desarrollo, top);
        return ResponseEntity.ok(ApiResponse.success("Top de cupones canjeados obtenido exitosamente", topCanjeados));
    }

    @PostMapping("/duplicar-masivo")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> duplicarMasivoCupones(
            @Valid @RequestBody DuplicarCuponesMasivoRequest request
    ) {
        String usuario = userContext.getUsername();
        cuponesService.duplicarMasivoCupones(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Clonación masiva de cupones completada exitosamente", null));
    }
}
