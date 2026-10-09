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

    Optional<SystemQr> findByQrToken(String qrToken);

    boolean existsByQrToken(String qrToken);

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
