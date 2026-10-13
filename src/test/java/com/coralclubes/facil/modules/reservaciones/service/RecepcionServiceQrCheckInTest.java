package com.coralclubes.facil.modules.reservaciones.service;

import com.coralclubes.facil.modules.reservaciones.dto.response.DetalleReservacionDto;
import com.coralclubes.facil.modules.reservaciones.dto.response.ResolucionQrCheckInResponse;
import com.coralclubes.facil.modules.reservaciones.repository.RecepcionRepository;
import com.coralclubes.facil.modules.reservaciones.repository.ReservacionesRepository;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResolucionResponse;
import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import com.coralclubes.facil.shared.platform.qr.service.SystemQrService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecepcionServiceQrCheckInTest {

    @Mock
    private RecepcionRepository recepcionRepo;

    @Mock
    private ReservacionesRepository reservacionesRepo;

    @Mock
    private SystemQrService systemQrService;

    @InjectMocks
    private RecepcionService recepcionService;

    @Test
    @DisplayName("Debe resolver QR con múltiples folios separados por dos puntos y comas")
    void testResolverQrMultiplesFolios() {
        String token = "abc123token";
        Integer desarrolloUsuario = 1;

        SystemQrResolucionResponse qr = SystemQrResolucionResponse.builder()
                .id(UUID.randomUUID())
                .token(token)
                .module("RESERVACIONES")
                .submodule("RESERVACIONES_RECEPCION")
                .actionType("CHECK_IN")
                .entityId("MEMB-001:101:102,103")
                .entityType("RESERVATION")
                .status(SystemQrStatus.ACTIVE)
                .build();

        when(systemQrService.resolverQrPorToken(token)).thenReturn(qr);

        DetalleReservacionDto det101 = DetalleReservacionDto.builder().membresia("MEMB-001").consecutivo(101).build();
        DetalleReservacionDto det102 = DetalleReservacionDto.builder().membresia("MEMB-001").consecutivo(102).build();
        DetalleReservacionDto det103 = DetalleReservacionDto.builder().membresia("MEMB-001").consecutivo(103).build();

        when(reservacionesRepo.obtenerDetalleReservacion("MEMB-001", 101)).thenReturn(det101);
        when(reservacionesRepo.obtenerDetalleReservacion("MEMB-001", 102)).thenReturn(det102);
        when(reservacionesRepo.obtenerDetalleReservacion("MEMB-001", 103)).thenReturn(det103);

        ResolucionQrCheckInResponse response = recepcionService.resolverQrParaCheckIn(token, desarrolloUsuario);

        assertNotNull(response);
        assertEquals("MEMB-001", response.membresia());
        assertEquals(token, response.token());
        assertEquals(3, response.reservaciones().size());
        assertEquals(101, response.reservaciones().get(0).consecutivo());
        assertEquals(102, response.reservaciones().get(1).consecutivo());
        assertEquals(103, response.reservaciones().get(2).consecutivo());
    }

    @Test
    @DisplayName("Debe rechazar QR si no está activo")
    void testResolverQrInactivo() {
        String token = "exp123token";
        SystemQrResolucionResponse qr = SystemQrResolucionResponse.builder()
                .status(SystemQrStatus.EXPIRED)
                .build();

        when(systemQrService.resolverQrPorToken(token)).thenReturn(qr);
        Integer desarrolloUsuario = 1;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                recepcionService.resolverQrParaCheckIn(token, desarrolloUsuario));

        assertTrue(ex.getMessage().contains("no está activo"));
    }

    @Test
    @DisplayName("Debe rechazar QR si no pertenece al dominio de reservaciones")
    void testResolverQrModuloInvalido() {
        String token = "otro123token";
        SystemQrResolucionResponse qr = SystemQrResolucionResponse.builder()
                .status(SystemQrStatus.ACTIVE)
                .module("COBRANZA")
                .entityType("PAYMENT")
                .build();

        when(systemQrService.resolverQrPorToken(token)).thenReturn(qr);
        Integer desarrolloUsuario = 1;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                recepcionService.resolverQrParaCheckIn(token, desarrolloUsuario));

        assertTrue(ex.getMessage().contains("no corresponde a una reservación"));
    }
}
