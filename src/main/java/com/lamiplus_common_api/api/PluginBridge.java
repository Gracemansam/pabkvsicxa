package com.lamiplus_common_api.api;



import com.lamiplus_common_api.api.PluginDataService;
import com.lamiplus_common_api.api.PluginDataServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PluginBridge {

    private PluginDataServiceRegistry registry;

    @Autowired(required = false)
    @Qualifier("corePluginDataServiceRegistry")
    public void setRegistry(PluginDataServiceRegistry registry) {
        this.registry = registry;
        log.info("PluginBridge initialized with registry: {}", registry != null);
    }


    public FindBuilder find(String entityName) {
        return new FindBuilder(entityName);
    }

    public SaveBuilder save(String entityName) {
        return new SaveBuilder(entityName);
    }


    public DeleteBuilder delete(String entityName) {
        return new DeleteBuilder(entityName);
    }


    public boolean isAvailable(String entityName) {
        return getService(entityName).isPresent();
    }



    public class FindBuilder {
        private final String entityName;

        FindBuilder(String entityName) {
            this.entityName = entityName;
        }

        /** Find a single entity by UUID */
        public Optional<Map<String, Object>> byUuid(UUID uuid) {
            return getService(entityName)
                    .flatMap(s -> s.findByUuid(uuid));
        }

        /** Find multiple entities by UUIDs */
        public List<Map<String, Object>> byUuids(List<UUID> uuids) {
            return getService(entityName)
                    .map(s -> s.findByUuids(uuids))
                    .orElse(Collections.emptyList());
        }

        /** Find all entities for a patient */
        public List<Map<String, Object>> byPatient(UUID patientUuid) {
            return getService(entityName)
                    .map(s -> s.findByPatientUuid(patientUuid))
                    .orElse(Collections.emptyList());
        }

        /** Find all entities for a tenant */
        public List<Map<String, Object>> byTenant(String tenantId) {
            return getService(entityName)
                    .map(s -> s.findByTenantId(tenantId))
                    .orElse(Collections.emptyList());
        }

        /** Find by patient + tenant */
        public List<Map<String, Object>> byPatientAndTenant(UUID patientUuid, String tenantId) {
            return getService(entityName)
                    .map(s -> s.findByPatientUuidAndTenantId(patientUuid, tenantId))
                    .orElse(Collections.emptyList());
        }

        /** Find by a custom field (e.g., "consultationUuid") */
        public List<Map<String, Object>> byField(String fieldName, Object value) {
            return getService(entityName)
                    .map(s -> s.findByField(fieldName, value))
                    .orElse(Collections.emptyList());
        }

        /** Find with pagination */
        public List<Map<String, Object>> paged(String tenantId, int page, int size) {
            return getService(entityName)
                    .map(s -> s.findAll(tenantId, page, size))
                    .orElse(Collections.emptyList());
        }


        public List<Map<String, Object>> byFieldFiltered(
                String fieldName, Object fieldValue,
                String filterField, String filterValue) {
            return byField(fieldName, fieldValue).stream()
                    .filter(d -> filterValue.equalsIgnoreCase(
                            String.valueOf(d.get(filterField))))
                    .collect(Collectors.toList());
        }
    }

    public class SaveBuilder {
        private final String entityName;
        private final Map<String, Object> data = new LinkedHashMap<>();

        SaveBuilder(String entityName) {
            this.entityName = entityName;
        }

        /** Add a field to save */
        public SaveBuilder field(String key, Object value) {
            if (value != null) {
                // Auto-convert UUIDs to strings for safe serialization
                data.put(key, value instanceof UUID ? value.toString() : value);
            }
            return this;
        }

        /** Add multiple fields at once */
        public SaveBuilder fields(Map<String, Object> fields) {
            fields.forEach(this::field);
            return this;
        }

        /** Execute the save and return the saved entity */
        public Map<String, Object> execute() {
            return getService(entityName)
                    .map(s -> {
                        log.debug("PluginBridge saving {} with fields: {}", entityName, data.keySet());
                        Map<String, Object> saved = s.save(data);
                        log.info("PluginBridge saved {}: {}", entityName, saved.get("uuid"));
                        return saved;
                    })
                    .orElseThrow(() -> new PluginServiceUnavailableException(entityName));
        }

        /** Execute the save, return Optional (won't throw if unavailable) */
        public Optional<Map<String, Object>> executeSafe() {
            try {
                return Optional.of(execute());
            } catch (PluginServiceUnavailableException e) {
                log.warn("Service unavailable for {}, skipping save", entityName);
                return Optional.empty();
            }
        }
    }

    // ========================
    // DELETE BUILDER
    // ========================

    public class DeleteBuilder {
        private final String entityName;

        DeleteBuilder(String entityName) {
            this.entityName = entityName;
        }

        /** Delete by UUID */
        public boolean byUuid(UUID uuid) {
            return getService(entityName)
                    .map(s -> s.deleteByUuid(uuid))
                    .orElse(false);
        }
    }

    // ========================
    // INTERNAL
    // ========================

    private Optional<PluginDataService> getService(String entityName) {
        if (registry == null) {
            log.warn("PluginBridge: registry not available, cannot access {}", entityName);
            return Optional.empty();
        }
        return registry.getServiceByEntity(entityName);
    }
}
