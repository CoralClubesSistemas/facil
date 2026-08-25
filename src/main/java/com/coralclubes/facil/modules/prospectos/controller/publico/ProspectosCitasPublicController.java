package com.coralclubes.facil.modules.prospectos.controller.publico;

import com.coralclubes.facil.modules.prospectos.dto.request.RegistrarResultadoCitaRequest;
import com.coralclubes.facil.modules.prospectos.service.ProspectosService;
import com.coralclubes.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador público para la recepción de resultados de citas de prospectos desde sistemas externos.
 */
@RestController
@RequestMapping("/api/v1/public/prospectos/citas")
@RequiredArgsConstructor
public class ProspectosCitasPublicController {

    private final ProspectosService prospectosService;

    /**
     * Endpoint para registrar el resultado de una cita y emitir el evento hacia Zoho CRM.
     *
     * @param request Datos del resultado de la cita (asistió/compró/no asistió y datos de compra).
     * @return Respuesta indicando si el evento fue procesado y despachado.
     */
    @PostMapping("/resultado")
    public ResponseEntity<ApiResponse<Boolean>> registrarResultadoCita(@Valid @RequestBody RegistrarResultadoCitaRequest request) {
        String usuario = request.usuario() != null && !request.usuario().isBlank() ? request.usuario() : "SISTEMA_EXTERNO";
        boolean resultado = prospectosService.procesarResultadoCita(request, usuario);
        return ResponseEntity.ok(ApiResponse.success("Resultado de cita procesado y evento emitido hacia Zoho correctamente", resultado));
    }
}
