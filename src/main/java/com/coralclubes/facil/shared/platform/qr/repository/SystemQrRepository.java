package com.coralclubes.facil.shared.platform.qr.repository;

import com.coralclubes.facil.shared.platform.qr.model.SystemQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemQrRepository extends JpaRepository<SystemQr, UUID> {

    Optional<SystemQr> findByQrToken(String qrToken);

    boolean existsByQrToken(String qrToken);
}
