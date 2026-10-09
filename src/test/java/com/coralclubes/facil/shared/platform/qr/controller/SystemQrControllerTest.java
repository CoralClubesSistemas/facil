package com.coralclubes.facil.shared.platform.qr.controller;

import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResolucionResponse;
import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import com.coralclubes.facil.shared.platform.qr.service.SystemQrService;
import com.coralclubes.responses.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemQrControllerTest {

    @Mock
    private SystemQrService systemQrService;

    @InjectMocks
    private SystemQrController systemQrController;

    @Test
    @DisplayName("Debe retornar ApiResponse con SystemQrResolucionResponse al invocar el endpoint")
    void testResolverQrPorTokenEndpoint() {
        String token = "tok123456789";
        UUID id = UUID.randomUUID();
        SystemQrResolucionResponse data = SystemQrResolucionResponse.builder()
                .id(id)
                .token(token)
                .module("RESERVACIONES")
                .submodule("RESERVACIONES_RECEPCION")
                .actionType("CHECK_IN")
                .entityId("MEMB-001:101")
                .entityType("RESERVATION")
                .status(SystemQrStatus.ACTIVE)
                .rutaModulo("/reservaciones/recepcion")
                .build();

        when(systemQrService.resolverQrPorToken(token)).thenReturn(data);

        ResponseEntity<ApiResponse<SystemQrResolucionResponse>> response = systemQrController.resolverQrPorToken(token);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Información del código QR recuperada exitosamente", response.getBody().message());
        assertEquals(token, response.getBody().data().token());
        assertEquals("RESERVACIONES", response.getBody().data().module());
        assertEquals("/reservaciones/recepcion", response.getBody().data().rutaModulo());

        verify(systemQrService).resolverQrPorToken(token);
    }
}
