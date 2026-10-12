package com.coralclubes.facil.shared.platform.qr.repository;

import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import com.coralclubes.facil.shared.platform.qr.model.SystemQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemQrRepository extends JpaRepository<SystemQr, UUID> {

    /**
     * Busca un QR por su token.
     *
     * @param qrToken El token del QR a buscar.
     * @return La entidad del QR si se encuentra, o Optional.empty() si no se encuentra.
     */
    Optional<SystemQr> findByQrToken(String qrToken);

    /**
     * Verifica si existe un QR con un token específico.
     *
     * @param qrToken El token del QR a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByQrToken(String qrToken);

    /**
     * Busca QRs activos por módulo, tipo de entidad y patrón de ID.
     *
     * @param module        El módulo del QR.
     * @param entityType    El tipo de entidad del QR.
     * @param status        El estado del QR.
     * @param exactEntityId El ID exacto de la entidad.
     * @param pattern       El patrón de búsqueda para el ID de la entidad.
     * @return La lista de QRs que cumplen con los criterios.
     */
    @Query("""
                SELECT q FROM SystemQr q
                WHERE q.module = :module
                  AND q.entityType = :entityType
                  AND q.status = :status
                  AND (q.entityId = :exactEntityId OR q.entityId LIKE :pattern)
                ORDER BY q.createdAt DESC
            """)
    List<SystemQr> buscarActivosPorModuloEntidadYPatron(
            @Param("module") String module,
            @Param("entityType") String entityType,
            @Param("status") SystemQrStatus status,
            @Param("exactEntityId") String exactEntityId,
            @Param("pattern") String pattern
    );
}
