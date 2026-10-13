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

        DetalleReservacionDto det101 = DetalleReservacionDto.builder().membresia("MEMB-001").consecutivo(101).desarrolloId(1).build();
        DetalleReservacionDto det102 = DetalleReservacionDto.builder().membresia("MEMB-001").consecutivo(102).desarrolloId(1).build();
        DetalleReservacionDto det103 = DetalleReservacionDto.builder().membresia("MEMB-001").consecutivo(103).desarrolloId(1).build();

        when(reservacionesRepo.obtenerDetalleReservacion("MEMB-001", 101)).thenReturn(det101);
        when(reservacionesRepo.obtenerDetalleReservacion("MEMB-001", 102)).thenReturn(det102);
        when(reservacionesRepo.obtenerDetalleReservacion("MEMB-001", 103)).thenReturn(det103);

        ResolucionQrCheckInResponse response = recepcionService.resolverQrParaCheckIn(token, desarrolloUsuario);

        assertNotNull(response);
        assertEquals("MEMB-001", response.membresia());
        assertEquals(token, response.token());
        assertTrue(response.consumible());
        assertEquals(3, response.reservaciones().size());
        assertEquals(101, response.reservaciones().get(0).consecutivo());
        assertEquals(102, response.reservaciones().get(1).consecutivo());
        assertEquals(103, response.reservaciones().get(2).consecutivo());
    }

    @Test
    @DisplayName("Debe aceptar QR con estatus USED devolviendo consumible = false y mensaje informativo")
    void testResolverQrUsed() {
        String token = "used123token";
        Integer desarrolloUsuario = 1;

        SystemQrResolucionResponse qr = SystemQrResolucionResponse.builder()
                .id(UUID.randomUUID())
                .token(token)
                .module("RESERVACIONES")
                .submodule("RESERVACIONES_RECEPCION")
                .actionType("CHECK_IN")
                .entityId("MEMB-001:101")
                .entityType("RESERVATION")
                .status(SystemQrStatus.USED)
                .maxUses(1)
                .usedCount(1)
                .build();

        when(systemQrService.resolverQrPorToken(token)).thenReturn(qr);

        DetalleReservacionDto det101 = DetalleReservacionDto.builder().membresia("MEMB-001").consecutivo(101).desarrolloId(1).build();
        when(reservacionesRepo.obtenerDetalleReservacion("MEMB-001", 101)).thenReturn(det101);

        ResolucionQrCheckInResponse response = recepcionService.resolverQrParaCheckIn(token, desarrolloUsuario);

        assertNotNull(response);
        assertEquals("MEMB-001", response.membresia());
        assertEquals(token, response.token());
        assertEquals(SystemQrStatus.USED, response.status());
        assertFalse(response.consumible());
        assertEquals(0, response.usosDisponibles());
        assertNotNull(response.mensaje());
        assertTrue(response.mensaje().contains("utilizado") || response.mensaje().contains("agotado"));
        assertEquals(1, response.reservaciones().size());
    }

    @Test
    @DisplayName("Debe aceptar QR con estatus REVOKED devolviendo consumible = false y mensaje amigable de cancelación")
    void testResolverQrRevoked() {
        String token = "rev123token";
        Integer desarrolloUsuario = 1;

        SystemQrResolucionResponse qr = SystemQrResolucionResponse.builder()
                .id(UUID.randomUUID())
                .token(token)
                .module("RESERVACIONES")
                .submodule("RESERVACIONES_RECEPCION")
                .actionType("CHECK_IN")
                .entityId("MEMB-001:101")
                .entityType("RESERVATION")
                .status(SystemQrStatus.REVOKED)
                .maxUses(1)
                .usedCount(0)
                .build();

        when(systemQrService.resolverQrPorToken(token)).thenReturn(qr);

        DetalleReservacionDto det101 = DetalleReservacionDto.builder().membresia("MEMB-001").consecutivo(101).desarrolloId(1).build();
        when(reservacionesRepo.obtenerDetalleReservacion("MEMB-001", 101)).thenReturn(det101);

        ResolucionQrCheckInResponse response = recepcionService.resolverQrParaCheckIn(token, desarrolloUsuario);

        assertNotNull(response);
        assertEquals("MEMB-001", response.membresia());
        assertEquals(token, response.token());
        assertEquals(SystemQrStatus.REVOKED, response.status());
        assertFalse(response.consumible());
        assertEquals(0, response.usosDisponibles());
        assertNotNull(response.mensaje());
        assertTrue(response.mensaje().contains("cancelado") || response.mensaje().contains("revocado"));
        assertEquals(1, response.reservaciones().size());
    }

    @Test
    @DisplayName("Debe rechazar QR si está expirado")
    void testResolverQrInactivo() {
        String token = "exp123token";
        SystemQrResolucionResponse qr = SystemQrResolucionResponse.builder()
                .status(SystemQrStatus.EXPIRED)
                .build();

        when(systemQrService.resolverQrPorToken(token)).thenReturn(qr);
        Integer desarrolloUsuario = 1;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                recepcionService.resolverQrParaCheckIn(token, desarrolloUsuario));

        assertTrue(ex.getMessage().contains("no es válido"));
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
