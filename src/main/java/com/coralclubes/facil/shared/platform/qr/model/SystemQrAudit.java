package com.coralclubes.facil.shared.platform.qr.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "system_qrs_audit")
public class SystemQrAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qr_id", nullable = false)
    private SystemQr qr;

    @Size(max = 50)
    @NotNull
    @Column(name = "\"action\"", nullable = false, length = 50)
    private String action;

    @Size(max = 30)
    @NotNull
    @Column(name = "status_result", nullable = false, length = 30)
    private String statusResult;

    @Size(max = 100)
    @NotNull
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Size(max = 50)
    @Column(name = "terminal_id", length = 50)
    private String terminalId;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @NotNull
    @ColumnDefault("sysutcdatetime()")
    @Column(name = "action_at", nullable = false)
    private Instant actionAt;

    @Nationalized
    @Lob
    @Column(name = "metadata")
    private String metadata;
}
