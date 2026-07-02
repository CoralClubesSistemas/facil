package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.projection.InformacionSocioDb;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioBusqueda;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioTabla;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SociosRepository {
    private final StoredProcedureExecutor executor;

    private final RowMapper<InformacionSocioDb> informacionSocioRowMapper = (rs, rowNum) ->
            new InformacionSocioDb(
                    rs.getString("membresia"),
                    rs.getString("nombreCompleto"),
                    rs.getString("nombre"),
                    rs.getString("segundoNombre"),
                    rs.getString("apellidoPaterno"),
                    rs.getString("apellidoMaterno"),
                    rs.getString("correo"),
                    rs.getString("correoAlternativo"),
                    rs.getString("telefono"),
                    rs.getString("telefonoAlternativo"),
                    rs.getDate("fechaNacimiento").toLocalDate(),
                    rs.getInt("tipoMembresiaId"),
                    rs.getString("tipoMembresia"),
                    rs.getInt("clasificacionMembresiaId"),
                    rs.getString("clasificacionMembresia"),
                    rs.getInt("desarrolloId"),
                    rs.getString("desarrollo"),
                    rs.getInt("estatusMembresiaId"),
                    rs.getString("estatusMembresia"),
                    rs.getInt("carteraCobranzaId"),
                    rs.getString("carteraCobranza"),
                    rs.getObject("vigenciaOriginal", Integer.class),
                    rs.getString("tiempoRestante"),
                    rs.getObject("AlertaConsultaActiva", Integer.class),
                    rs.getObject("AlertaConsultaConsecutivo", Integer.class),
                    rs.getString("AlertaConsultaNota"),
                    rs.getTimestamp("AlertaConsultaFechaRegistro") != null
                            ? rs.getTimestamp("AlertaConsultaFechaRegistro").toLocalDateTime()
                            : null,
                    rs.getString("direccion")
            );

    private final RowMapper<InformacionSocioBusqueda> informacionSocioBusquedaRowMapper = (rs, rowNum) ->
            InformacionSocioBusqueda.builder()
                    .membresia(rs.getString("membresia"))
                    .nombreCompleto(rs.getString("nombreCompleto"))
                    .correo(rs.getString("correo"))
                    .telefono(rs.getString("telefono"))
                    .tipoMembresia(rs.getString("tipoMembresia"))
                    .clasificacionMembresia(rs.getString("clasificacionMembresia"))
                    .desarrollo(rs.getString("desarrollo"))
                    .estatusMembresia(rs.getString("estatusMembresia"))
                    .carteraCobranza(rs.getString("carteraCobranza"))
                    .build();

    private final RowMapper<InformacionSocioTabla> informacionSocioTablaRowMapper = (rs, rowNum) ->
            InformacionSocioTabla.builder()
                    // Identidad
                    .membresia(rs.getString("membresia"))
                    .nombreCompleto(rs.getString("nombreCompleto"))
                    .nombre(rs.getString("nombre"))
                    .segundoNombre(rs.getString("segundoNombre"))
                    .apellidoPaterno(rs.getString("apellidoPaterno"))
                    .apellidoMaterno(rs.getString("apellidoMaterno"))
                    .fechaNacimiento(rs.getDate("fechaNacimiento").toLocalDate())

                    // Comunicación
                    .correo(rs.getString("correo"))
                    .correoAlternativo(rs.getString("correoAlternativo"))
                    .telefono(rs.getString("telefono"))
                    .telefonoAlternativo(rs.getString("telefonoAlternativo"))

                    // Indicadores Financieros y Cobranza
                    .saldoFinMes(rs.getBigDecimal("saldoFinMes"))
                    .tipoTarjetaAfiliada(rs.getString("tipoTarjetaAfiliada"))
                    .ejecutivoAsignado(rs.getString("ejecutivoAsignado"))
                    .ultimoPQAPagado(rs.getString("ultimoPQAPagado"))

                    // Puntos
                    .puntosDisponibles(rs.getBigDecimal("puntosDisponibles"))
                    .puntosConsumidos(rs.getBigDecimal("puntosConsumidos"))

                    // Beneficiarios
                    .totalBenefActivos(rs.getObject("totalBenefActivos", Integer.class))
                    .nombresBeneficiarios(rs.getString("nombresBeneficiarios"))

                    // Configuración de Membresía
                    .tipoMembresiaId(rs.getObject("tipoMembresiaId", Integer.class))
                    .tipoMembresia(rs.getString("tipoMembresia"))
                    .clasificacionMembresiaId(rs.getObject("clasificacionMembresiaId", Integer.class))
                    .clasificacionMembresia(rs.getString("clasificacionMembresia"))

                    // Desarrollo y Estatus
                    .desarrolloId(rs.getObject("desarrolloId", Integer.class))
                    .desarrollo(rs.getString("desarrollo"))
                    .estatusMembresiaId(rs.getObject("estatusMembresiaId", Integer.class))
                    .estatusMembresia(rs.getString("estatusMembresia"))

                    // Cartera y Vigencia
                    .carteraCobranzaId(rs.getObject("carteraCobranzaId", Integer.class))
                    .carteraCobranza(rs.getString("carteraCobranza"))
                    .vigenciaOriginal(rs.getString("vigenciaOriginal"))
                    .tiempoRestante(rs.getString("tiempoRestante"))

                    // Metadatos de Paginación
                    .totalRegistros(rs.getObject("totalRegistros", Integer.class))
                    .build();

    public Optional<InformacionSocioDb> spClientesObtenerDatosSocio(String membresia) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        return executor.querySingle("spClientesObtenerDatosSocio", params, informacionSocioRowMapper);
    }

    public List<InformacionSocioBusqueda> spClientesBusquedaInteligente(String busqueda) {
        Map<String, Object> params = new HashMap<>();
        params.put("Busqueda", busqueda);
        return executor.queryList("spClientesBusquedaInteligente", params, informacionSocioBusquedaRowMapper);
    }

    public List<InformacionSocioTabla> spFacilBusquedaPorFiltros(
            String membresia,
            String nombre,
            String nombreBeneficiario,
            Integer desarrolloId,
            Integer tipoMembresiaId,
            Integer clasificacionMembresiaId,
            Integer carteraCobranzaId,
            Integer estatusMembresiaId,
            String email,
            String telefono,
            Integer pagina,
            Integer tamanioPagina
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("Nombre", nombre);
        params.put("NombreBeneficiario", nombreBeneficiario);
        params.put("DesarrolloId", desarrolloId);
        params.put("TipoMembresiaId", tipoMembresiaId);
        params.put("ClasificacionMembresiaId", clasificacionMembresiaId);
        params.put("CarteraCobranzaId", carteraCobranzaId);
        params.put("EstatusMembresiaId", estatusMembresiaId);
        params.put("Email", email);
        params.put("Telefono", telefono);
        params.put("Pagina", pagina);
        params.put("TamanoPagina", tamanioPagina);

        return executor.queryList("spFacilBusquedaPorFiltros", params, informacionSocioTablaRowMapper);
    }
}
