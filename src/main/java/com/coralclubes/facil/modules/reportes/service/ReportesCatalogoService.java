package com.coralclubes.facil.modules.reportes.service;

import com.coralclubes.dto.SelectGenerico;
import com.coralclubes.facil.modules.reportes.enums.ClavesModulosReportes;
import com.coralclubes.facil.modules.reportes.repository.ReportesCatalogoRepository;
import com.coralclubes.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportesCatalogoService {

    private final ReportesCatalogoRepository repository;

    public ApiResponse<List<SelectGenerico<Integer>>> obtenerCatalogo(String nombreCatalogo) {
        List<SelectGenerico<Integer>> datos = switch (nombreCatalogo.toUpperCase()) {
            case "DESARROLLOS" -> repository.catalogoDesarrollos();
            case "CARTERAS" -> repository.catalogoCartera();
            case "CLASIFICACIONMEMBRESIA" -> repository.catalogoClasificacionMembresia();
            case "EMPLEADOS" -> repository.catalogoEmpleados();
            case "ESTADOS" -> repository.catalogoEstados();
            case "ESTATUSRECIBOS" -> repository.catalogoEstatusRecibos();
            case "ESTATUSUNIDADES" -> repository.catalogoEstatusUnidades();
            case "LOCACIONES" -> repository.catalogoLocaciones();
            case "PROMOTORES" -> repository.catalogoPromotores();
            case "SERIESRECIBOS" -> repository.catalogoSeriesRecibos();
            case "TIPOSCUPONESDESCUENTOPQA" -> repository.catalogoTipoCupones();
            case "TIPOSMOVIMIENTOS" -> repository.catalogoTiposMovimientos();
            case "TIPOSPRODUCTOS" -> repository.catalogoTiposProductos();
            case "USUARIOS" -> repository.catalogoUsuarios();
            case "ESTATUSRESERVACIONES" -> repository.catalogoEstatusReservaciones();
            case "ESTATUSMEMBRESIA" -> repository.catalogoEstatusMembresia();
            case "TIPOMEMBRESIA" -> repository.catalogoTiposMembresias();
            default -> throw new IllegalArgumentException("Catálogo no encontrado: " + nombreCatalogo);
        };

        return ApiResponse.success("Catálogo obtenido", datos);
    }

    public String resolverClaveModulo(ClavesModulosReportes modulo) {
        return modulo.getClave();
    }
}
