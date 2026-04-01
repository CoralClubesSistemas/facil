package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.modules.reservaciones.dto.response.CamaristaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.InventarioBodegaDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.SugerenciaAmenidadDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.TareaDashboardDto;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AmaDeLlavesRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<CamaristaDto> camaristaMapper = (rs, rowNum) -> new CamaristaDto(
            rs.getInt("idCamarista"),
            rs.getString("nombre"),
            rs.getInt("idDesarrollo"),
            rs.getBoolean("activo")
    );

    // --- ROW MAPPERS ---
    private final RowMapper<TareaDashboardDto> tareaMapper = (rs, rowNum) -> new TareaDashboardDto(
            rs.getInt("idTarea"),
            rs.getInt("idUnidadFisica"),
            rs.getString("numeroHabitacion"),
            rs.getString("tipoHabitacion"),
            rs.getInt("cantidadPersonas"),
            rs.getString("peticionEspecial"),
            rs.getObject("idCamarista", Integer.class), // Puede ser null
            rs.getString("nombreCamarista"),
            rs.getString("claveEstatus"),
            rs.getTimestamp("fechaCreacion").toLocalDateTime(),
            rs.getTimestamp("fechaInicioLimpieza") != null ? rs.getTimestamp("fechaInicioLimpieza").toLocalDateTime() : null
    );

    private final RowMapper<SugerenciaAmenidadDto> sugerenciaMapper = (rs, rowNum) -> new SugerenciaAmenidadDto(
            rs.getInt("idArticulo"),
            rs.getString("nombreArticulo"),
            rs.getString("unidadMedida"),
            rs.getInt("cantidadSugerida")
    );

    private final RowMapper<InventarioBodegaDto> inventarioMapper = (rs, rowNum) -> new InventarioBodegaDto(
            rs.getInt("idAlmacen"),
            rs.getString("nombreAlmacen"),
            rs.getInt("idArticulo"),
            rs.getString("skuSicofi"),
            rs.getString("nombreArticulo"),
            rs.getString("unidadMedida"),
            rs.getInt("stockActual")
    );

    public List<CamaristaDto> obtenerCamaristas(Integer idDesarrollo) {
        return spExecutor.queryList("spAmaObtenerCamaristas", Map.of("IdDesarrollo", idDesarrollo), camaristaMapper);
    }

    public void guardarCamarista(Integer idCamarista, String nombre, Integer idDesarrollo, String usuario) {
        spExecutor.execute("spAmaGuardarCamarista", Map.of(
                "IdCamarista", idCamarista,
                "Nombre", nombre,
                "IdDesarrollo", idDesarrollo,
                "Usuario", usuario
        ));
    }

    public void desactivarCamarista(Integer idCamarista, String usuario) {
        spExecutor.execute("spAmaDesactivarCamarista", Map.of(
                "IdCamarista", idCamarista,
                "Usuario", usuario
        ));
    }

    public List<TareaDashboardDto> obtenerTareasDashboard(Integer idDesarrollo) {
        return spExecutor.queryList("spAmaObtenerTareasDashboard", Map.of("IdDesarrollo", idDesarrollo), tareaMapper);
    }

    public void cambiarEstatusTarea(Integer idTarea, String nuevoEstatus, Integer idCamarista, String usuario) {
        spExecutor.execute("spAmaCambiarEstatusTarea", Map.of(
                "IdTarea", idTarea,
                "NuevoEstatus", nuevoEstatus,
                "IdCamarista", idCamarista,
                "Usuario", usuario
        ));
    }

    public List<SugerenciaAmenidadDto> obtenerSugerenciaAmenidades(Integer idTarea) {
        return spExecutor.queryList("spAmaObtenerSugerenciaAmenidades", Map.of("IdTarea", idTarea), sugerenciaMapper);
    }

    public List<InventarioBodegaDto> obtenerInventarioBodega(Integer idDesarrollo) {
        return spExecutor.queryList("spAmaObtenerInventarioBodega", Map.of("IdDesarrollo", idDesarrollo), inventarioMapper);
    }

    public void finalizarTarea(Integer idTarea, Integer idAlmacenOrigen, String jsonConsumos, String usuario) {
        spExecutor.execute("spAmaFinalizarTarea", Map.of(
                "IdTarea", idTarea,
                "IdAlmacenOrigen", idAlmacenOrigen,
                "JsonConsumos", jsonConsumos,
                "Usuario", usuario
        ));
    }

    public void crearTareaLimpieza(Integer idUnidadFisica, String usuario, String origenAccion) {
        spExecutor.execute("spAmaCrearTareaLimpieza", Map.of(
                "IdUnidadFisica", idUnidadFisica,
                "Usuario", usuario,
                "OrigenAccion", origenAccion
        ));
    }
}