package com.lamiplus_common_api.common;

import com.lamiplus_common_api.audit.*;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(UniversalAuditListener.class)
public class BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "uuid")
    @AuditUUID
    private UUID uuid;

    @Column(name = "created_at", nullable = false, updatable = false)
    @AuditCreatedDate
    private LocalDateTime createdAt;

    @AuditCreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @AuditUpdatedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @AuditUpdatedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "archived")
    @AuditArchived
    private Integer archived = 0;

    @Column(name = "tenant_id", length = 50)
    @AuditTenant
    private String tenantId;

    @Column(name = "facility_id")
    @AuditFacility
    private UUID facilityId;
}