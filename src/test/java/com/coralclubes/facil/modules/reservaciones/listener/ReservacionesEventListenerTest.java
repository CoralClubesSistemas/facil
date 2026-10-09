package com.coralclubes.facil.modules.reservaciones.listener;

import com.coralclubes.facil.modules.reservaciones.dto.response.ResultadoCartaOcupacion;
import com.coralclubes.facil.modules.reservaciones.service.ReservacionesService;
import com.coralclubes.facil.shared.events.dto.ReservacionConfirmadaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservacionesEventListenerTest {

    @Mock
    private ReservacionesService reservacionesService;

    @InjectMocks
    private ReservacionesEventListener eventListener;

    @Test
    @DisplayName("Debe orquestar la generación de carta de ocupación y envío de notificación con ambos UUIDs")
    void testProcesarCartaOcupacionExitoso() {
        ReservacionConfirmadaEvent event = ReservacionConfirmadaEvent.builder()
                .membresia("MEMB-001")
                .foliosGenerados(List.of(101))
                .nombreReserva("Juan Pérez")
                .email("juan@test.com")
                .fechaEntrada(LocalDate.now().plusDays(2))
                .fechaSalida(LocalDate.now().plusDays(5))
                .subtotal(BigDecimal.valueOf(1500))
                .habitaciones(List.of())
                .build();

        UUID uuidPdf = UUID.randomUUID();
        UUID qrFileId = UUID.randomUUID();
        ResultadoCartaOcupacion resultado = new ResultadoCartaOcupacion(uuidPdf, qrFileId);

        when(reservacionesService.construirEventDesdeDb("MEMB-001", 101)).thenReturn(event);
        when(reservacionesService.generarYPersistirCartaOcupacion(any(ReservacionConfirmadaEvent.class))).thenReturn(resultado);

        eventListener.procesarCartaOcupacion(event);

        verify(reservacionesService).generarYPersistirCartaOcupacion(any(ReservacionConfirmadaEvent.class));
        verify(reservacionesService).enviarNotificacionCartaOcupacion(any(ReservacionConfirmadaEvent.class), eq(uuidPdf), eq(qrFileId), eq(List.of()));
    }
}
