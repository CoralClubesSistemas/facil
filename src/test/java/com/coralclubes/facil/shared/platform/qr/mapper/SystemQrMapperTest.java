package com.coralclubes.facil.shared.platform.qr.mapper;

import com.coralclubes.facil.shared.platform.qr.dto.CreateQrRequest;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResponse;
import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import com.coralclubes.facil.shared.platform.qr.model.SystemQr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SystemQrMapperTest {

    private final SystemQrMapper mapper = Mappers.getMapper(SystemQrMapper.class);

    @Test
    @DisplayName("Debe transformar CreateQrRequest aplicando valores por defecto cuando vienen nulos")
    void debeMapearConValoresPorDefecto() {
        CreateQrRequest request = CreateQrRequest.builder()
                .module("RESERVACIONES")
                .actionType("CHECK_IN")
                .entityId("MEMB-001:10")
                .entityType("RESERVATION")
                .build();

        String token = "RESERVACIONES_CHECK_IN_token123";
        UUID fileId = null;

        SystemQr entity = mapper.toEntity(request, token, fileId);

        assertNotNull(entity);
        assertNotNull(entity.getId(), "El ID debe ser autogenerado");
        assertEquals(7, entity.getId().version(), "El ID autogenerado debe ser un UUID versión 7");
        assertEquals(token, entity.getQrToken());
        assertNull(entity.getQrFileId());
        assertEquals("RESERVACIONES", entity.getModule());
        assertNull(entity.getSubmodule());
        assertEquals("CHECK_IN", entity.getActionType());
        assertEquals("MEMB-001:10", entity.getEntityId());
        assertEquals("RESERVATION", entity.getEntityType());

        // Verificación de defaults
        assertEquals(SystemQrStatus.ACTIVE, entity.getStatus());
        assertEquals(1, entity.getMaxUses());
        assertEquals(0, entity.getUsedCount());
        assertNull(entity.getExpiresAt());
        assertNull(entity.getLastUsedAt());
        assertEquals("SYSTEM", entity.getCreatedBy());
        assertNotNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getUpdatedBy());
    }

    @Test
    @DisplayName("Debe transformar CreateQrRequest respetando los valores explícitos proporcionados")
    void debeMapearConValoresExplicitos() {
        UUID fileId = UUID.randomUUID();
        Instant expira = Instant.now().plus(7, ChronoUnit.DAYS);

        CreateQrRequest request = CreateQrRequest.builder()
                .module("COBRANZA")
                .submodule("CAJA")
                .actionType("PAYMENT_RECEIPT")
                .entityId("FOLIO-999")
                .entityType("PAYMENT")
                .urlAlmacenamiento("cobranza/qrs/2026")
                .status(SystemQrStatus.REVOKED)
                .maxUses(5)
                .expiresAt(expira)
                .metadata("{\"terminal\":\"T-01\"}")
                .createdBy("admin_usr")
                .build();

        String token = "COBRANZA_PAYMENT_RECEIPT_custom999";
        SystemQr entity = mapper.toEntity(request, token, fileId);

        assertNotNull(entity);
        assertEquals(token, entity.getQrToken());
        assertEquals(fileId, entity.getQrFileId());
        assertEquals("COBRANZA", entity.getModule());
        assertEquals("CAJA", entity.getSubmodule());
        assertEquals("PAYMENT_RECEIPT", entity.getActionType());
        assertEquals("FOLIO-999", entity.getEntityId());
        assertEquals("PAYMENT", entity.getEntityType());
        assertEquals(SystemQrStatus.REVOKED, entity.getStatus());
        assertEquals(5, entity.getMaxUses());
        assertEquals(0, entity.getUsedCount());
        assertEquals(expira, entity.getExpiresAt());
        assertEquals("{\"terminal\":\"T-01\"}", entity.getMetadata());
        assertEquals("admin_usr", entity.getCreatedBy());
    }

    @Test
    @DisplayName("Debe transformar SystemQr a SystemQrResponse con URL de descarga")
    void debeMapearAResponse() {
        SystemQr entity = new SystemQr();
        entity.setId(UUID.randomUUID());
        entity.setQrToken("token-test");
        entity.setModule("RESERVACIONES");
        entity.setActionType("CHECK_IN");
        entity.setEntityId("MEMB-1");
        entity.setEntityType("RESERVATION");
        entity.setStatus(SystemQrStatus.ACTIVE);
        entity.setMaxUses(1);
        entity.setUsedCount(0);
        entity.setCreatedAt(Instant.now());
        entity.setCreatedBy("SYSTEM");

        SystemQrResponse response = mapper.toResponse(entity, "https://storage.coralclubes.com/descarga/qr.png");

        assertNotNull(response);
        assertEquals(entity.getId(), response.id());
        assertEquals("token-test", response.qrToken());
        assertEquals("https://storage.coralclubes.com/descarga/qr.png", response.urlDescarga());
    }
}
