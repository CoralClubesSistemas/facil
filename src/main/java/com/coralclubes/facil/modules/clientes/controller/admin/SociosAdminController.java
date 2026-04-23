package com.coralclubes.facil.modules.clientes.controller.admin;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioBusqueda;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioTabla;
import com.coralclubes.facil.modules.clientes.service.SociosService;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/clientes/socios")
@RequiredArgsConstructor
public class SociosAdminController {
    private final SociosService service;

    @GetMapping("/busqueda/socio/{membresia}")
    public ResponseEntity<ApiResponse<InformacionSocio>> obtenerSocio(
            @PathVariable String membresia
    ) {
        return ResponseEntity.ok(service.obtenerSocios(membresia));
    }

    @GetMapping("/busqueda/{busqueda}")
    public ResponseEntity<ApiResponse<List<InformacionSocioBusqueda>>> obtenerSociosBusquedaRapida(
            @PathVariable String busqueda
    ) {
        ApiResponse<List<InformacionSocioBusqueda>> response = service.obtenerSociosBusquedaRapida(busqueda);
        return ResponseEntity.status(response.status()).body(response);
    }

    @GetMapping("/busqueda/filtros")
    public ResponseEntity<ApiResponse<List<InformacionSocioTabla>>> obtenerSociosPorFiltros(
            @RequestParam(required = false) String membresia,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String nombreBeneficiario,
            @RequestParam(required = false) Integer desarrolloId,
            @RequestParam(required = false) Integer tipoMembresiaId,
            @RequestParam(required = false) Integer clasificacionMembresiaId,
            @RequestParam(required = false) Integer carteraCobranzaId,
            @RequestParam(required = false) Integer estatusMembresiaId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telefono,
            @RequestParam(defaultValue = "1") Integer pagina,
            @RequestParam(defaultValue = "50") Integer tamanioPagina
    ) {
        ApiResponse<List<InformacionSocioTabla>> response = service.obtenerSociosPorFiltros(
                membresia,
                nombre,
                nombreBeneficiario,
                desarrolloId,
                tipoMembresiaId,
                clasificacionMembresiaId,
                carteraCobranzaId,
                estatusMembresiaId,
                email,
                telefono,
                pagina,
                tamanioPagina
        );

        return ResponseEntity.status(response.status()).body(response);
    }
}
