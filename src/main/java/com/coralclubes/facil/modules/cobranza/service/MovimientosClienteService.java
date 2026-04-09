package com.coralclubes.facil.modules.cobranza.service;

import com.coralclubes.facil.modules.cobranza.dto.request.EstadoCuentaAdeudoRequest;
import com.coralclubes.facil.modules.cobranza.dto.response.EstadoCuentaAdeudoDto;
import com.coralclubes.facil.modules.cobranza.repository.MovimientosClienteRepository;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientosClienteService {

    private final MovimientosClienteRepository repository;

    public ApiResponse<List<EstadoCuentaAdeudoDto>> obtenerEstadoCuentaAdeudo(EstadoCuentaAdeudoRequest request) {
        return ApiResponse.success(
                "Estado de cuenta por adeudo obtenido con éxito.",
                repository.spFacilObtenerEstadoCuentaAdeudo(request)
        );
    }
}

