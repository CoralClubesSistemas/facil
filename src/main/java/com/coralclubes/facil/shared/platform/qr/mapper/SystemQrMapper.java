package com.coralclubes.facil.shared.platform.qr.mapper;

import com.coralclubes.facil.shared.platform.qr.dto.CreateQrRequest;
import com.coralclubes.facil.shared.platform.qr.dto.SystemQrResponse;
import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import com.coralclubes.facil.shared.platform.qr.model.SystemQr;
import com.coralclubes.facil.shared.utils.UuidUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

/**
 * Mapper MapStruct para transformar DTOs y entidades de códigos QR.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = {SystemQrStatus.class, UuidUtils.class})
public interface SystemQrMapper {

    @Mapping(target = "id", expression = "java(UuidUtils.generateV7())")
    @Mapping(target = "qrToken", source = "qrToken")
    @Mapping(target = "qrFileId", source = "qrFileId")
    @Mapping(target = "status", expression = "java(request.status() != null ? request.status() : SystemQrStatus.ACTIVE)")
    @Mapping(target = "maxUses", expression = "java(request.maxUses() != null && request.maxUses() > 0 ? request.maxUses() : 1)")
    @Mapping(target = "usedCount", constant = "0")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "createdBy", expression = "java(request.createdBy() != null && !request.createdBy().isBlank() ? request.createdBy().trim() : \"SYSTEM\")")
    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    SystemQr toEntity(CreateQrRequest request, String qrToken, UUID qrFileId);

    @Mapping(target = "urlDescarga", source = "urlDescarga")
    SystemQrResponse toResponse(SystemQr entity, String urlDescarga);

    default SystemQrResponse toResponse(SystemQr entity) {
        return toResponse(entity, null);
    }
}
