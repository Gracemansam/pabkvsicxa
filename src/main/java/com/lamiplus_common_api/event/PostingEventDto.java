package com.lamiplus_common_api.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostingEventDto {
    private UUID postingId;
    private String status;
    private UUID patientUuid;
    private UUID visitUuid;
    private String serviceName;
    private UUID serviceUuid;
    private String tenantId;
    private UUID facilityId;
}