package com.lamiplus_common_api.common;

import lombok.Builder;

import java.util.UUID;

@Builder
public class TenantContextData {
    private String tenantId;
    private UUID facilityId;
    private UUID userId;

    public TenantContextData(String tenantId, UUID facilityId, UUID userId) {
        this.tenantId = tenantId;
        this.facilityId = facilityId;
        this.userId = userId;
    }

    public String getTenantId() { return tenantId; }
    public UUID getFacilityId() { return facilityId; }

    public UUID getUserId() { return userId; }
}
