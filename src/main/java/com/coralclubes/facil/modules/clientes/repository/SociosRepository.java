package com.coralclubes.facil.modules.clientes.repository;

import com.coralclubes.facil.modules.clientes.dto.projection.InformacionSocioDb;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioBusqueda;
import com.coralclubes.facil.modules.clientes.dto.response.InformacionSocioTabla;
import com.coralclubes.facil.modules.clientes.dto.response.DatosSocioResponse;
import com.coralclubes.facil.modules.clientes.dto.response.DomicilioSocioDto;
import com.coralclubes.facil.modules.clientes.dto.response.MembresiaTarjetaDto;
import com.coralclubes.utils.json.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
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

    private final RowMapper<DatosSocioResponse> datosSocioRowMapper = (rs, rowNum) -> {
        String domiciliosJson = rs.getString("domicilios");
        List<DomicilioSocioDto> listDomicilios = java.util.Collections.emptyList();
        if (domiciliosJson != null && !domiciliosJson.isBlank()) {
            try {
                listDomicilios = JsonUtils.fromJson(
                        domiciliosJson,
                        new TypeReference<List<DomicilioSocioDto>>() {}
                );
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        return DatosSocioResponse.builder()
                .membresia(rs.getString("membresia"))
                .fechaNacimiento(rs.getDate("fechaNacimiento") != null ? rs.getDate("fechaNacimiento").toLocalDate() : null)
                .edad(rs.getObject("edad") != null ? rs.getInt("edad") : null)
                .rfc(rs.getString("rfc"))
                .curp(rs.getString("curp"))
                .genero(rs.getString("genero"))
                .estadoCivil(rs.getString("estadoCivil"))
                .ocupacion(rs.getString("ocupacion"))
                .estatusCliente(rs.getString("estatusCliente"))
                .mailPersonal(rs.getString("mailPersonal"))
                .mailTrabajo(rs.getString("mailTrabajo"))
                .fechaRegistro(rs.getTimestamp("fechaRegistro") != null ? rs.getTimestamp("fechaRegistro").toLocalDateTime() : null)
                .nombreCompleto(rs.getString("nombreCompleto"))
                .nombre(rs.getString("primerNombre"))
                .segundoNombre(rs.getString("segundoNombre"))
                .apellidoPaterno(rs.getString("apellidoPaterno"))
                .apellidoMaterno(rs.getString("apellidoMaterno"))
                .nombreTitularAdicional(rs.getString("nombreTitularAdicional"))
                .domicilios(listDomicilios)
                .build();
    };

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

    public Optional<DatosSocioResponse> spMembresiaObtenerDatosSocio(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);
        return executor.querySingle("spMembresiaObtenerDatosSocio", params, datosSocioRowMapper);
    }

    private final RowMapper<MembresiaTarjetaDto> membresiaTarjetaRowMapper = (rs, rowNum) ->
            MembresiaTarjetaDto.builder()
                    .tipoFranquicia(rs.getString("tipo_franquicia"))
                    .tarjeta(rs.getString("tarjeta"))
                    .idInstrumento(rs.getObject("id_instrumento", Integer.class))
                    .tipoTarjeta(rs.getString("tipo_tarjeta"))
                    .clabe(rs.getString("clabe"))
                    .vigencia(rs.getString("vigencia"))
                    .idBanco(rs.getObject("id_banco", Integer.class))
                    .banco(rs.getString("banco"))
                    .codigoSeguridad(rs.getString("codigo_seguridad"))
                    .idPrioridad(rs.getObject("id_prioridad", Integer.class))
                    .prioridad(rs.getString("prioridad"))
                    .esTitularDiferente(rs.getBoolean("es_titular_diferente"))
                    .nombreTitular(rs.getString("nombre_titular"))
                    .exentarCargoAutomatico(rs.getBoolean("exentar_cargo_automatico"))
                    .idEstatus(rs.getObject("id_estatus", Integer.class))
                    .estatusTarjeta(rs.getString("estatus_tarjeta"))
                    .usuarioRegistro(rs.getString("usuario_registro"))
                    .fechaActualizacion(rs.getString("fecha_actualizacion"))
                    .build();

    public List<MembresiaTarjetaDto> spClienteObtenerTarjetas(String membresia) {
        Map<String, Object> params = Map.of("Membresia", membresia);
        return executor.queryList("spClienteObtenerTarjetas", params, membresiaTarjetaRowMapper);
    }
}
