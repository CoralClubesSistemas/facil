package com.coralclubes.facil.modules.clientes.listener;

import com.coralclubes.facil.modules.clientes.dto.request.ConsumoPuntosRequest;
import com.coralclubes.facil.modules.clientes.service.PuntosService;
import com.coralclubes.facil.shared.events.dto.ConsumoPuntosReservacionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PuntosEventListener {

    private final PuntosService puntosService;

    @EventListener
    public void manejarConsumoPuntosReservacion(ConsumoPuntosReservacionEvent event) {
        ConsumoPuntosRequest request = ConsumoPuntosRequest.builder()
                .membresia(event.membresia())
                .desarrolloId(event.desarrolloId())
                .totalPuntos(event.totalPuntos())
                .puntosHospedaje(event.totalPuntos())
                .puntosInstalaciones(0)
                .puntosCampoGolf(0)
                .idMovimiento(event.idMovimiento())
                .descripcion(event.descripcion())
                .usuario(event.usuario())
                .build();
        puntosService.consumirPuntos(request);
    }
}
