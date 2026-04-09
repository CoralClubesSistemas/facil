package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.modules.clientes.repository.SociosRepository;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SociosService {
    private final SociosRepository repo;

    public ApiResponse<List<InformacionSocio>> obtenerSocios(String busqueda) {
        return ApiResponse.success(
                "Socios obtenidos exitosamente",
                repo.spFacilBusquedaInteligente(busqueda)
        );
    }

    public ApiResponse<List<InformacionSocio>> obtenerSociosPorFiltros(
            String membresia,
            String nombre,
            Integer desarrolloId,
            Integer tipoMembresiaId,
            Integer clasificacionMembresiaId,
            Integer carteraCobranzaId,
            Integer estatusMembresiaId,
            String email,
            String telefono
    ) {
        return ApiResponse.success(
                "Socios obtenidos por filtros exitosamente",
                repo.spFacilBusquedaPorFiltros(
                        membresia,
                        nombre,
                        desarrolloId,
                        tipoMembresiaId,
                        clasificacionMembresiaId,
                        carteraCobranzaId,
                        estatusMembresiaId,
                        email,
                        telefono
                )
        );
    }
}
