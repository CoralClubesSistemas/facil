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
}
