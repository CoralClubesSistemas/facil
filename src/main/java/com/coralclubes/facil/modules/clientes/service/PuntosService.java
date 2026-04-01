package com.coralclubes.facil.modules.clientes.service;

import com.coralclubes.facil.modules.clientes.dto.request.ConsumoPuntosRequest;
import com.coralclubes.facil.modules.clientes.dto.response.PuntosMembresia;
import com.coralclubes.facil.modules.clientes.repository.PuntosRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PuntosService {
    private final PuntosRepository repo;

    public PuntosMembresia obtenerPuntosMembresia(String membresia) {
        return repo.spSaldoPuntosDisponiblesMembresia(membresia);
    }

    public Integer consumirPuntos(ConsumoPuntosRequest request) {
        int pHospedaje = request.puntosHospedaje() != null ? request.puntosHospedaje() : 0;
        int pInstalaciones = request.puntosInstalaciones() != null ? request.puntosInstalaciones() : 0;
        int pGolf = request.puntosCampoGolf() != null ? request.puntosCampoGolf() : 0;

        if ((pHospedaje + pInstalaciones + pGolf) != request.totalPuntos()) {
            throw new IllegalArgumentException("La suma del desglose de puntos (" + (pHospedaje + pInstalaciones + pGolf) + ") no coincide con el total solicitado (" + request.totalPuntos() + ").");
        }

        // 2. Validar que el cliente tenga saldo
        PuntosMembresia saldoActual = obtenerPuntosMembresia(request.membresia());
        if (saldoActual == null || saldoActual.saldoPuntosNeto() < request.totalPuntos()) {
            throw new IllegalArgumentException("La membresía no cuenta con los puntos suficientes. Saldo actual: " + (saldoActual != null ? saldoActual.saldoPuntosNeto() : 0));
        }

        // 3. Ejecutar el Stored Procedure
        return repo.spCliConsumirPuntos(request);
    }
}
