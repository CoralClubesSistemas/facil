package com.coralclubes.facil.shared.platform.qr.model;

import com.coralclubes.facil.shared.platform.qr.enums.SystemQrStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "system_qrs")
public class SystemQr {
    @Id
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 128)
    @NotNull
    @Column(name = "qr_token", nullable = false, length = 128)
    private String qrToken;

    @Column(name = "qr_file_id")
    private UUID qrFileId;

    @Size(max = 50)
    @NotNull
    @Column(name = "\"module\"", nullable = false, length = 50)
    private String module;

    @Size(max = 50)
    @Column(name = "submodule", length = 50)
    private String submodule;

    @Size(max = 50)
    @NotNull
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Size(max = 64)
    @NotNull
    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @Size(max = 50)
    @NotNull
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Enumerated(EnumType.STRING)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private SystemQrStatus status;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Nationalized
    @Lob
    @Column(name = "metadata")
    private String metadata;

    @NotNull
    @ColumnDefault("sysutcdatetime()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Size(max = 100)
    @NotNull
    @ColumnDefault("'SYSTEM'")
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Size(max = 100)
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = com.coralclubes.facil.shared.utils.UuidUtils.generateV7();
        }
        if (this.status == null) {
            this.status = SystemQrStatus.ACTIVE;
        }
        if (this.maxUses == null || this.maxUses <= 0) {
            this.maxUses = 1;
        }
        if (this.usedCount == null) {
            this.usedCount = 0;
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.createdBy == null || this.createdBy.isBlank()) {
            this.createdBy = "SYSTEM";
        }
    }
}
