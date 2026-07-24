package com.coralclubes.facil.modules.cobranza.controller.admin;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.cobranza.dto.request.GuardarCuponRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponListadoResponse;
import com.coralclubes.facil.modules.cobranza.dto.response.CuponesCatalogoElementoResponse;
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
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoOrigenes() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de orígenes obtenido exitosamente", cuponesService.obtenerCatalogoOrigenes()));
    }

    @GetMapping("/catalogos/destinos")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<List<SelectGenerico<Integer>>>> obtenerCatalogoDestinos() {
        return ResponseEntity.ok(ApiResponse.success("Catálogo de destinos obtenido exitosamente", cuponesService.obtenerCatalogoDestinos()));
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
            @RequestParam(required = false) Integer origen,
            @RequestParam(required = false) Integer destino
    ) {
        List<CuponListadoResponse> listado = cuponesService.obtenerListadoCupones(year, desarrollo, origen, destino);
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
            @RequestParam Integer idCupon,
            @RequestParam String uuidArchivo) {
        cuponesService.guardarImagenCupon(idCupon, uuidArchivo);
        return ResponseEntity.ok(ApiResponse.empty());
    }

    @GetMapping("/imagen")
    public ResponseEntity<ApiResponse<ArchivoDescarga>> obtenerFormatoCupon (
            @RequestParam UUID uuid) {
        ArchivoDescarga archivo = cuponesService.obtenerFormatoCupon(uuid);
        return ResponseEntity.ok(ApiResponse.success("Archivo obtenido correctamente", archivo));
    }

    @DeleteMapping("/{idCupon}")
    @PreAuthorize("hasAuthority('MOD_MNUCOBRANZA')")
    public ResponseEntity<ApiResponse<Void>> eliminarCupon(@PathVariable Integer idCupon) {
        cuponesService.eliminarCupon(idCupon);
        return ResponseEntity.ok(ApiResponse.success("Cupón eliminado correctamente", null));
    }
}
