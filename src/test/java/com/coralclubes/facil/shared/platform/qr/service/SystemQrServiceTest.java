package com.coralclubes.facil.shared.platform.qr.service;

import com.coralclubes.facil.shared.infrastructure.integration.storage.StorageClient;
import com.coralclubes.facil.shared.infrastructure.integration.storage.dto.InfoArchivoDto;
import com.coralclubes.facil.shared.platform.qr.dto.CreateQrRequest;
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
        assertTrue(response.qrToken().startsWith("RESERVACIONES_CHECK_IN_"));
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
                entity.getQrToken().startsWith("COBRANZA_PAYMENT_RECEIPT_") &&
                entity.getId().version() == 7
        ));
    }
}
