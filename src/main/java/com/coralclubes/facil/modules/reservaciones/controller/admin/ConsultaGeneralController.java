package com.coralclubes.facil.modules.reservaciones.controller.admin;

import com.coralclubes.facil.modules.reservaciones.dto.request.FiltroConsultaGeneral;
import com.coralclubes.facil.modules.reservaciones.dto.response.ReservacionHistoricaDto;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.domain.dto.PaginaResponse;
import com.coralclubes.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reservaciones")
public class ConsultaGeneralController {

    private final ReservacionesService service;

    public ConsultaGeneralController(ReservacionesService service) {
        this.service = service;
    }

    @PostMapping("/historico")
    public ResponseEntity<ApiResponse<PaginaResponse<ReservacionHistoricaDto>>> consultarHistorico(
            @RequestBody FiltroConsultaGeneral filtro) {

        var response = service.consultarHistorico(filtro);
        return ResponseEntity.ok(response);
    }
}