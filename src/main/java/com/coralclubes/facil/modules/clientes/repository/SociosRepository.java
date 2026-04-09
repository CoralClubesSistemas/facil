package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocio;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SociosRepository {
    private final StoredProcedureExecutor executor;

    private final RowMapper<InformacionSocio> informacionSocioRowMapper = (rs, rowNum) ->
            InformacionSocio.builder()
                    .membresia(rs.getString("membresia"))
                    .nombreCompleto(rs.getString("nombreCompleto"))
                    .nombre(rs.getString("nombre"))
                    .segundoNombre(rs.getString("segundoNombre"))
                    .apellidoPaterno(rs.getString("apellidoPaterno"))
                    .apellidoMaterno(rs.getString("apellidoMaterno"))
                    .correo(rs.getString("correo"))
                    .correoAlternativo(rs.getString("correoAlternativo"))
                    .telefono(rs.getString("telefono"))
                    .telefonoAlternativo(rs.getString("telefonoAlternativo"))
                    .fechaNacimiento(rs.getDate("fechaNacimiento").toLocalDate())
                    .tipoMembresiaId(rs.getInt("tipoMembresiaId"))
                    .tipoMembresia(rs.getString("tipoMembresia"))
                    .clasificacionMembresiaId(rs.getInt("clasificacionMembresiaId"))
                    .clasificacionMembresia(rs.getString("clasificacionMembresia"))
                    .desarrolloId(rs.getInt("desarrolloId"))
                    .desarrollo(rs.getString("desarrollo"))
                    .estatusMembresiaId(rs.getInt("estatusMembresiaId"))
                    .estatusMembresia(rs.getString("estatusMembresia"))
                    .carteraCobranzaId(rs.getInt("carteraCobranzaId"))
                    .carteraCobranza(rs.getString("carteraCobranza"))
                    .vigenciaOriginal(rs.getInt("vigenciaOriginal"))
                    .vigenciaRestante(rs.getString("tiempoRestante"))
                    .build();

    public List<InformacionSocio> spFacilBusquedaInteligente(String busqueda) {
        Map<String, Object> params = new HashMap<>();
        params.put("Busqueda", busqueda);
        return executor.queryList("spFacilBusquedaInteligente", params, informacionSocioRowMapper);
    }

    public List<InformacionSocio> spFacilBusquedaPorFiltros(
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
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Nombre", nombre);
        params.put("DesarrolloId", desarrolloId);
        params.put("TipoMembresiaId", tipoMembresiaId);
        params.put("ClasificacionMembresiaId", clasificacionMembresiaId);
        params.put("CarteraCobranzaId", carteraCobranzaId);
        params.put("EstatusMembresiaId", estatusMembresiaId);
        params.put("Email", email);
        params.put("Telefono", telefono);

        return executor.queryList("spFacilBusquedaPorFiltros", params, informacionSocioRowMapper);
    }
}
