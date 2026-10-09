package com.coralclubes.facil.shared.platform.qr.service;

import com.coralclubes.facil.modules.sistema.dto.projection.ModuloDetalleProjection;
import com.coralclubes.facil.modules.sistema.service.ModulosService;
import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.platform.qr.dto.CreateQrRequest;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResolucionResponse;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResponse;
import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import com.coralclubes.facil.shared.platform.qr.mapper.SystemQrMapper;
import com.coralclubes.facil.shared.platform.qr.model.SystemQr;
import com.coralclubes.facil.shared.platform.qr.repository.SystemQrRepository;
import com.coralclubes.facil.shared.utils.QrCodeService;
import com.coralclubes.logging.BusinessLogger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemQrServiceTest {

    @Mock
    private SystemQrRepository systemQrRepository;

    @Spy
    private SystemQrMapper systemQrMapper = Mappers.getMapper(SystemQrMapper.class);

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private StorageClient storageClient;

    @Mock
    private ModulosService modulosService;

    @Mock
    private BusinessLogger businessLogger;

    @InjectMocks
    private SystemQrService systemQrService;

    @Test
    @DisplayName("Debe crear QR, subir a Storage y persistir en BD")
    void debeCrearQrConStorageYPersistir() {
        CreateQrRequest request = CreateQrRequest.builder()
                .module("RESERVACIONES")
                .actionType("CHECK_IN")
                .entityId("MEMB-001:10")
                .entityType("RESERVATION")
                .urlAlmacenamiento("reservaciones/cartas-ocupacion")
                .build();

        byte[] fakeBytes = new byte[]{1, 2, 3, 4};
        when(qrCodeService.generarQr(anyString())).thenReturn(fakeBytes);

        UUID fakeFileId = UUID.randomUUID();
        InfoArchivoDto fakeInfo = new InfoArchivoDto(
                fakeFileId,
                "QR_test.png",
                "png",
                "image/png",
                1024L,
                "DISPONIBLE",
                false,
                "https://storage.coralclubes.com/files/" + fakeFileId,
                java.time.OffsetDateTime.now()
        );
        when(storageClient.cargarArchivoSincrono(eq(fakeBytes), anyString(), eq("image/png"), any())).thenReturn(fakeInfo);

        when(systemQrRepository.save(any(SystemQr.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemQrResponse response = systemQrService.crearQr(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals(7, response.id().version(), "El ID debe ser UUID versión 7");
        assertNotNull(response.qrToken());
        assertTrue(response.qrToken().matches("^[a-zA-Z0-9]+$"), "El token debe contener solo caracteres alfanuméricos");
        assertEquals(fakeFileId, response.qrFileId());
        assertEquals("https://storage.coralclubes.com/files/" + fakeFileId, response.urlDescarga());
        assertEquals(SystemQrStatus.ACTIVE, response.status());

        verify(storageClient, times(1)).cargarArchivoSincrono(eq(fakeBytes), anyString(), eq("image/png"), any());
        verify(systemQrRepository, times(1)).save(any(SystemQr.class));
    }

    @Test
    @DisplayName("Debe generar QR en bytes y persistir en BD sin llamar a Storage")
    void debeCrearQrEnBytesSinStorage() {
        CreateQrRequest request = CreateQrRequest.builder()
                .module("COBRANZA")
                .actionType("PAYMENT_RECEIPT")
                .entityId("REC-12345")
                .entityType("PAYMENT")
                .build();

        byte[] fakeBytes = new byte[]{9, 8, 7, 6};
        when(qrCodeService.generarQr(anyString())).thenReturn(fakeBytes);
        when(systemQrRepository.save(any(SystemQr.class))).thenAnswer(invocation -> invocation.getArgument(0));

        byte[] resultadoBytes = systemQrService.crearQrSinStorage(request);

        assertNotNull(resultadoBytes);
        assertArrayEquals(fakeBytes, resultadoBytes);

        // Verificar que NO se invocó el cliente de almacenamiento
        verify(storageClient, never()).cargarArchivoSincrono(any(), any(), any(), any());
        // Pero SÍ se guardó en la base de datos
        verify(systemQrRepository, times(1)).save(argThat(entity ->
                entity.getQrFileId() == null &&
                entity.getQrToken().matches("^[a-zA-Z0-9]+$") &&
                entity.getId().version() == 7
        ));
    }

    @Test
    @DisplayName("Debe obtener QR activo por módulo y patrón de entidad (soporta carritos)")
    void debeObtenerQrActivoPorModuloYPatronEntidad() {
        SystemQr entity = new SystemQr();
        entity.setId(UUID.randomUUID());
        entity.setQrToken("TOKEN-123");
        entity.setModule("RESERVACIONES");
        entity.setEntityType("RESERVATION");
        entity.setEntityId("MEMB-001:101,102");
        entity.setStatus(SystemQrStatus.ACTIVE);
        UUID fakeQrFileId = UUID.randomUUID();
        entity.setQrFileId(fakeQrFileId);

        when(systemQrRepository.buscarActivosPorModuloEntidadYPatron(
                eq("RESERVACIONES"),
                eq("RESERVATION"),
                eq(SystemQrStatus.ACTIVE),
                eq("MEMB-001:101"),
                eq("MEMB-001:%101%")
        )).thenReturn(java.util.List.of(entity));

        var resultadoOpt = systemQrService.obtenerQrActivoPorModuloYEntidad(
                "RESERVACIONES",
                "RESERVATION",
                "MEMB-001:101",
                "MEMB-001:%101%"
        );

        assertTrue(resultadoOpt.isPresent());
        assertEquals("MEMB-001:101,102", resultadoOpt.get().entityId());
        assertEquals(fakeQrFileId, resultadoOpt.get().qrFileId());
    }

    @Test
    @DisplayName("Debe resolver QR por token y obtener información del módulo desde ModulosService")
    void debeResolverQrPorTokenYModuloExitosamente() {
        String token = "abc1234567890def";
        SystemQr entity = new SystemQr();
        entity.setId(UUID.randomUUID());
        entity.setQrToken(token);
        entity.setModule("RESERVACIONES");
        entity.setSubmodule("RESERVACIONES_RECEPCION");
        entity.setActionType("CHECK_IN");
        entity.setEntityType("RESERVATION");
        entity.setEntityId("MEMB-001:101");
        entity.setStatus(SystemQrStatus.ACTIVE);

        when(systemQrRepository.findByQrToken(token)).thenReturn(Optional.of(entity));

        ModuloDetalleProjection moduloProjection = ModuloDetalleProjection.builder()
                .id(10L)
                .clave("smnuRecepcion")
                .padreId(5L)
                .clavePadre("mnuControlDeReservaciones")
                .nombre("Recepción")
                .ruta("/reservaciones/recepcion")
                .icono("hotel")
                .menuFacil(1)
                .menuFacilDescripcion("Menú de Reservaciones")
                .build();

        when(modulosService.obtenerModuloPorClave("mnuControlDeReservaciones", "smnuRecepcion"))
                .thenReturn(Optional.of(moduloProjection));

        SystemQrResolucionResponse respuesta = systemQrService.resolverQrPorToken(token);

        assertNotNull(respuesta);
        assertEquals(entity.getId(), respuesta.id());
        assertEquals(token, respuesta.token());
        assertEquals("RESERVACIONES", respuesta.module());
        assertEquals("RESERVACIONES_RECEPCION", respuesta.submodule());
        assertEquals("CHECK_IN", respuesta.actionType());
        assertEquals("MEMB-001:101", respuesta.entityId());
        assertEquals("RESERVATION", respuesta.entityType());
        assertEquals(SystemQrStatus.ACTIVE, respuesta.status());
        assertEquals("/reservaciones/recepcion", respuesta.rutaModulo());

        verify(modulosService).obtenerModuloPorClave("mnuControlDeReservaciones", "smnuRecepcion");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el token no existe al resolver QR")
    void debeLanzarExcepcionSiTokenNoExiste() {
        String tokenInexistente = "token_no_existe";
        when(systemQrRepository.findByQrToken(tokenInexistente)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> systemQrService.resolverQrPorToken(tokenInexistente));
    }
}
