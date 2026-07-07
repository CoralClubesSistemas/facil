package com.coralclubes.facil.modules.sistema.repository;

import com.coralclubes.facil.shared.infrastructure.repository.StoredProcedureExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LimpiezaMovimientosRepository {

    private final StoredProcedureExecutor spExecutor;

    public void sp_limpiar_movimientos_internet() {
        spExecutor.execute("sp_limpiar_movimientos_internet", Map.of());
    }
}
