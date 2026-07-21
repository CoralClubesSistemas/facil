package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.clientes.dto.request.CrearNotaUsuarioRequest;
import com.coralclubes.facil.modules.clientes.dto.response.CrearNotaUsuarioResponse;
import com.coralclubes.facil.modules.clientes.dto.response.NotasBuzonResponse;
import com.coralclubes.facil.modules.clientes.dto.response.NotasClienteResponse;
import com.coralclubes.facil.modules.clientes.dto.response.ObtenerArchivosNotaResponse;
import com.coralclubes.facil.modules.clientes.service.NotasClientesService;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.RespuestaCargaDto;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.SolicitarUrlRequest;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/socios/notas")
@RequiredArgsConstructor
public class NotasClientesAdminController {
    private final NotasClientesService service;

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<NotasClienteResponse>>> buscarNotasCliente(
            @RequestParam String numeroMembresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaRangoInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaRangoFinal,
            @RequestParam(required = false) Integer clasificaNota
    ) {
        return ResponseEntity.ok(service.buscarNotasCliente(numeroMembresia, fechaRangoInicial, fechaRangoFinal, clasificaNota));
    }

    @GetMapping("/clasificaciones-rol")
    public ResponseEntity<List<SelectGenerico<Integer>>> obtenerClasificacionesXUsuario() {
        ApiResponse<List<SelectGenerico<Integer>>> response = service.obtenerClasificacionesXUsuario();
        return ResponseEntity.ok(response.data());
    }

    @PostMapping("/crear")
    public ResponseEntity<ApiResponse<CrearNotaUsuarioResponse>> crearNota(
            @Valid @RequestBody CrearNotaUsuarioRequest request) {
        return ResponseEntity.ok(service.crearNota(request));
    }

    @PostMapping("/{membresia}/{consecutivo}/archivos/solicitar-urls")
    public ResponseEntity<ApiResponse<List<RespuestaCargaDto>>> solicitarUrlsDeCargaArchivos(
            @PathVariable String membresia,
            @PathVariable Integer consecutivo,
            @Valid @RequestBody List<SolicitarUrlRequest> solicitudes) {
        return ResponseEntity.ok(service.solicitarUrlsDeCargaArchivos(membresia, consecutivo, solicitudes));
    }

    @PostMapping("/{membresia}/{consecutivo}/archivos/registrar")
    public ResponseEntity<ApiResponse<Void>> registrarArchivoNota(
            @PathVariable String membresia,
            @PathVariable Integer consecutivo,
            @RequestParam String nombreArchivo,
            @RequestParam String uuidArchivo,
            @RequestParam String tipoArchivo) {
        service.registrarArchivoNota(membresia, consecutivo, nombreArchivo, uuidArchivo, tipoArchivo);
        return ResponseEntity.ok(ApiResponse.success("Archivo registrado exitosamente.", null));
    }

    @GetMapping("/{membresia}/{consecutivo}/archivos")
    public ResponseEntity<ApiResponse<List<ObtenerArchivosNotaResponse>>> obtenerArchivosNota(
            @PathVariable String membresia,
            @PathVariable Integer consecutivo) {
        return ResponseEntity.ok(service.obtenerArchivosNota(membresia, consecutivo));
    }

    @PostMapping("/{membresia}/{consecutivo}/alerta/desactivar")
    public ResponseEntity<ApiResponse<Boolean>> desactivarNotaConAlerta(
            @PathVariable String membresia,
            @PathVariable Integer consecutivo) {
        return ResponseEntity.ok(service.desactivarNotaConAlerta(membresia, consecutivo));
    }

    @GetMapping("/buzon")
    public ResponseEntity<ApiResponse<List<NotasBuzonResponse>>> obtenerNotasBuzon(
            @RequestParam String membresia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer tipoNota
    ) {
        List<NotasBuzonResponse> notas = service.obtenerNotasBuzon(membresia, fechaDesde, fechaHasta, tipoNota);
        return ResponseEntity.ok(ApiResponse.success("Notas de buzón obtenidas correctamente.", notas));
    }
}
