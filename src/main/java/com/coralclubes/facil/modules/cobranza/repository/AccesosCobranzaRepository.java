package com.coralclubes.facil.modules.cobranza.repository;

import com.coralclubes.facil.modules.cobranza.dto.response.BeneficiarioAccesoVigenteResponse;
import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccesosCobranzaRepository {

    private final StoredProcedureExecutor spExecutor;

    private final RowMapper<BeneficiarioAccesoVigenteResponse> accesoVigenteRowMapper = (rs, rowNum) ->
            BeneficiarioAccesoVigenteResponse.builder()
                    .membresia(rs.getString("BACP_BEN_MEM_MEMBRESIA"))
                    .numBeneficiario(rs.getObject("BACP_BEN_NUMBENEFICIARIO") != null ? rs.getInt("BACP_BEN_NUMBENEFICIARIO") : null)
                    .estatusAcceso(rs.getObject("BACP_LSV_ESTATUS_ACCESO") != null ? rs.getInt("BACP_LSV_ESTATUS_ACCESO") : null)
                    .motivo(rs.getObject("BACP_LSV_MOTIVO") != null ? rs.getInt("BACP_LSV_MOTIVO") : null)
                    .motivoDescripcion(rs.getString("MOTIVO_DESCRIPCION"))
                    .notaRecomendaciones(rs.getString("BACP_NOTA_RECOMENDACIONES"))
                    .fechaInicio(rs.getTimestamp("BACP_FECHA_INICIO") != null ? rs.getTimestamp("BACP_FECHA_INICIO").toLocalDateTime() : null)
                    .fechaFinal(rs.getTimestamp("BACP_FECHA_FINAL") != null ? rs.getTimestamp("BACP_FECHA_FINAL").toLocalDateTime() : null)
                    .usuarioRegistra(rs.getString("BACP_USR_USUARIO_REGISTRA"))
                    .fechaRegistro(rs.getTimestamp("BACP_FECHA_REGISTRO") != null ? rs.getTimestamp("BACP_FECHA_REGISTRO").toLocalDateTime() : null)
                    .build();

    public void sp_InsertarBeneficiarioAccesoPreferencial(
            String membresia,
            Integer numBeneficiario,
            Integer estatusAcceso,
            Integer motivo,
            String notaRecomendaciones,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFinal,
            String usuarioRegistra
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("Membresia", membresia);
        params.put("NumBeneficiario", numBeneficiario);
        params.put("EstatusAcceso", estatusAcceso);
        params.put("Motivo", motivo);
        params.put("NotaRecomendaciones", notaRecomendaciones);
        params.put("FechaInicio", fechaInicio);
        params.put("FechaFinal", fechaFinal != null ? fechaFinal : LocalDateTime.of(2999, 12, 31, 23, 59, 59));
        params.put("UsuarioRegistra", usuarioRegistra);

        spExecutor.execute("sp_InsertarBeneficiarioAccesoPreferencial", params);
    }

    public Optional<BeneficiarioAccesoVigenteResponse> sp_ObtenerBeneficiarioAccesoVigente(String membresia, Integer numBeneficiario) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "NumBeneficiario", numBeneficiario
        );

        return spExecutor.querySingle("sp_ObtenerBeneficiarioAccesoVigente", params, accesoVigenteRowMapper);
    }

    public void sp_BajaBeneficiarioAccesoPreferencial(String membresia, Integer numBeneficiario, String usuarioModifica) {
        Map<String, Object> params = Map.of(
                "Membresia", membresia,
                "NumBeneficiario", numBeneficiario,
                "UsuarioModifica", usuarioModifica
        );

        spExecutor.execute("sp_BajaBeneficiarioAccesoPreferencial", params);
    }
}
