package com.lamiplus_common_api.event;

import com.lamiplus_common_api.common.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceCompletionPublisher {

    private final GenericEventPublisher eventPublisher;

    /**
     * Call this after saving ANY service entity across ANY plugin.
     *
     * @param postingId   from the request DTO
     * @param serviceUuid UUID of the saved entity
     * @param serviceName logical name e.g. "TETANUS_IMMUNIZATION", "ART_ENROLLMENT", "HTS_RESULT"
     * @param patientUuid patient context
     * @param visitUuid   visit context
     */
    public void publishCompletion(UUID postingId,
                                  UUID serviceUuid,
                                  String serviceName,
                                  UUID patientUuid,
                                  UUID visitUuid) {
        if (postingId == null) {
            log.warn("postingId is null — skipping POSTING_SERVICE_COMPLETED for serviceName='{}'", serviceName);
            return;
        }
        try {
            eventPublisher.publish("POSTING_SERVICE_COMPLETED",
                    PostingEventDto.builder()
                            .postingId(postingId)
                            .status("COMPLETED")
                            .serviceUuid(serviceUuid)
                            .serviceName(serviceName)
                            .patientUuid(patientUuid)
                            .visitUuid(visitUuid)
                            .tenantId(Utils.getTenantIdFromContext())
                            .facilityId(Utils.getFacilityIdFromContext())
                            .build()
            );
            log.info("✓ POSTING_SERVICE_COMPLETED — serviceName='{}' serviceUuid='{}' postingId='{}'",
                    serviceName, serviceUuid, postingId);
        } catch (Exception e) {
            log.error("Failed to publish POSTING_SERVICE_COMPLETED — serviceName='{}' postingId='{}': {}",
                    serviceName, postingId, e.getMessage(), e);
        }
    }
}