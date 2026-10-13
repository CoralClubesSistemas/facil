package com.coralclubes.facil.modules.reservaciones.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservacionesDocumentosRepository {

    private final StoredProcedureExecutor spExecutor;

    public void spResvGuardarUuidCartaOcupacion(String membresia, Integer consecutivo, String uuid) {
        Map<String, Object> params = Map.of(
                "membresia", membresia,
                "consecutivo", consecutivo,
                "uuid", uuid
        );
        spExecutor.execute("spResvGuardarUuidCartaOcupacion", params);
    }

    public Optional<String> spResvObtenerUuidCartaOcupacion(String membresia, Integer consecutivo) {
        Map<String, Object> params = Map.of(
                "membresia", membresia,
                "consecutivo", consecutivo
        );
        RowMapper<String> mapper = (rs, rowNum) -> rs.getString(1);
        return spExecutor.querySingle("spResvObtenerUuidCartaOcupacion", params, mapper);
    }

    public Integer spResvGuardarTarjetaRegistro(String membresia, Integer consecutivo, String usuario) {
        Map<String, Object> params = Map.of(
                "membresia", membresia,
                "consecutivo", consecutivo,
                "usuario", usuario
        );

        return spExecutor.querySingle(
                "spResvGuardarTarjetaRegistro",
                params,
                (rs, rowNum) -> rs.getInt("id")
        ).orElseThrow(() -> new IllegalStateException("No se pudo registrar la tarjeta de registro para la reservación."));
    }
}
