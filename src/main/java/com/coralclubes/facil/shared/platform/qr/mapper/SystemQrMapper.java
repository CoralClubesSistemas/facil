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

    // Genera un UUID con nuestra implementacion de uuid v7
    @Mapping(target = "id", expression = "java(UuidUtils.generateV7())")
    @Mapping(target = "qrToken", source = "qrToken")
    @Mapping(target = "qrFileId", source = "qrFileId")

    // Valida si los campos son null establece valores por defecto, si no los toma del request
    @Mapping(target = "status", expression = "java(request.status() != null ? request.status() : SystemQrStatus.ACTIVE)")
    @Mapping(target = "maxUses", expression = "java(request.maxUses() != null && request.maxUses() > 0 ? request.maxUses() : 1)")
    @Mapping(target = "usedCount", constant = "0")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "createdBy", expression = "java(request.createdBy() != null && !request.createdBy().isBlank() ? request.createdBy().trim() : \"SYSTEM\")")

    // Ignora los campos que no deben ser mapeados desde el request
    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    SystemQr toEntity(CreateQrRequest request, String qrToken, UUID qrFileId);

    /*
     * Convierte una entidad de QR a un DTO de respuesta.
     * @param entity Entidad de QR a convertir.
     * @param urlDescarga URL de descarga del QR.
     * @return DTO de respuesta del QR.
     */
    @Mapping(target = "urlDescarga", source = "urlDescarga")
    SystemQrResponse toResponse(SystemQr entity, String urlDescarga);

    default SystemQrResponse toResponse(SystemQr entity) {
        return toResponse(entity, null);
    }
}
