package com.coralclubes.facil.shared.platform.qr.repository;

import com.coralclubes.facil.shared.platform.qr.model.SystemQrAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemQrAuditRepository extends JpaRepository<SystemQrAudit, Long> {
}
