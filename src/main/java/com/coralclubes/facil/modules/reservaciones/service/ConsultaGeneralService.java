package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.request.FiltroConsultaGeneral;
import com.coralclubes.facil.modules.reservaciones.dto.response.ReservacionHistoricaDto;
import com.coralclubes.facil.modules.reservaciones.repository.ConsultaGeneralRepository;
import com.coralclubes.facil.shared.domain.dto.PaginaResponse;
import com.coralclubes.facil.shared.infrastructure.security.service.UserContext;
import com.coralclubes.responses.ApiResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaGeneralService {

    private final ConsultaGeneralRepository repository;
    private final UserContext userContext;

    public ConsultaGeneralService(ConsultaGeneralRepository repository, UserContext userContext) {
        this.repository = repository;
        this.userContext = userContext;
    }

    public ApiResponse<PaginaResponse<ReservacionHistoricaDto>> consultarHistorico(FiltroConsultaGeneral filtro) {
        var filtroConDesarrollo = new FiltroConsultaGeneral(
                userContext.getIdDesarrollo(),
                filtro.fechaInicio(),
                filtro.fechaFin(),
                filtro.tipoFecha(),
                filtro.estatusClave(),
                filtro.busqueda(),
                filtro.pageNumber(),
                filtro.pageSize()
        );

        List<ReservacionHistoricaDto> resultados = repository.consultarHistoricoReservaciones(filtroConDesarrollo);

        // Si la lista está vacía, el total es 0. Si trae datos, tomamos el total del primer registro.
        Integer totalRegistros = resultados.isEmpty() ? 0 : resultados.getFirst().totalRegistros();

        PaginaResponse<ReservacionHistoricaDto> pagina = new PaginaResponse<>(
                resultados,
                totalRegistros,
                filtro.pageNumber(),
                filtro.pageSize()
        );

        return ApiResponse.success("Consulta histórica completada", pagina);
    }
}