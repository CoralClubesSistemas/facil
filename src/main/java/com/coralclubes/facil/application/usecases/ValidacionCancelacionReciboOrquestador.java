package com.coralclubes.facil.application.usecases;

import com.coralclubes.facil.modules.cobranza.dto.response.AccionRequeridaDto;
import com.coralclubes.facil.modules.cobranza.dto.response.ValidacionCancelacionReciboResponse;
import com.coralclubes.facil.modules.cobranza.service.RecibosService;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.infrastructure.domain.codes.MovimientosEnum;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidacionCancelacionReciboOrquestador {
    private final RecibosService recibosService;
    private final ReservacionesService reservacionesService;

    public ValidacionCancelacionReciboResponse validar(Integer numeroRecibo, Integer serieId, String membresia) {
        List<String> bloqueos = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        List<AccionRequeridaDto> acciones = new ArrayList<>();

        boolean requiereConfirmacion = false;

        var recibo = recibosService.obtenerDetallesRecibo(numeroRecibo, serieId, membresia).data();

        for (var mov : recibo.movimientos()) {
            if (mov.tipoMovimiento().equals(MovimientosEnum.RESERVACIONES.getId())) {
                var reserva = reservacionesService.obtenerResumenReservacionXMovimiento(membresia, mov.idMovimiento());

                // Se evalúa cada reserva para ver si levanta bloqueos, advertencias o requiere confirmar
                switch (reserva.estatusClave()) {
                    case "CHECK-IN" -> advertencias.add("La reserva " + reserva.consecutivo() + " está en CHECK-IN. Se cancelará el recibo, pero esta reserva se mantendrá activa.");
                    case "CHECK-OUT" -> bloqueos.add("La reserva " + reserva.consecutivo() + " está en CHECK-OUT. Imposible cancelar recibo.");
                    case "PENDIENTE", "CONFIRMADA" -> {
                        if (reserva.importeTotal().compareTo(mov.totalNeto()) == 0) {
                            requiereConfirmacion = true; // Levantamos la bandera
                        }
                    }
                }
            }
        }

        // Si al menos una reserva necesita decisión, enviamos la acción global
        if (requiereConfirmacion && bloqueos.isEmpty()) {
            acciones.add(new AccionRequeridaDto(
                    "CANCELAR_RESERVAS",
                    "El recibo incluye reservaciones. ¿Desea cancelar TODAS las reservaciones asociadas?",
                    false
            ));
        }

        boolean permite = bloqueos.isEmpty();
        return new ValidacionCancelacionReciboResponse(permite, bloqueos, advertencias, acciones);
    }
}