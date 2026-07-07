package com.coralclubes.facil.modules.sistema.service;

import com.coralclubes.facil.modules.sistema.repository.LimpiezaMovimientosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LimpiezaMovimientosService {

    private final LimpiezaMovimientosRepository repository;

    public void limpiarMovimientosInternet() {
        repository.sp_limpiar_movimientos_internet();
    }
}
