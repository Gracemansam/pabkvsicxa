package com.lamiplus_common_api.api;



import java.util.*;

/**
 * Generic CRUD interface for cross-plugin data access.
 * All plugins implement this for their entities.
 * Uses Map<String, Object> for classloader compatibility.
 */
public interface PluginDataService {

    /**
     * Get the entity name this service handles (e.g., "Diagnosis", "Admission")
     */
    String getEntityName();

    // ============ READ OPERATIONS ============

    /**
     * Find entity by UUID
     */
    Optional<Map<String, Object>> findByUuid(UUID uuid);

    /**
     * Find multiple entities by UUIDs
     */
    List<Map<String, Object>> findByUuids(List<UUID> uuids);

    /**
     * Find all entities by patient UUID
     */
    List<Map<String, Object>> findByPatientUuid(UUID patientUuid);

    /**
     * Find all entities by tenant ID
     */
    List<Map<String, Object>> findByTenantId(String tenantId);

    /**
     * Find all entities by patient UUID and tenant ID
     */
    List<Map<String, Object>> findByPatientUuidAndTenantId(UUID patientUuid, String tenantId);

    /**
     * Find all entities (paginated)
     */
    List<Map<String, Object>> findAll(String tenantId, int page, int size);

    // ============ WRITE OPERATIONS ============

    /**
     * Save entity (create or update)
     * Returns saved entity as Map
     */
    Map<String, Object> save(Map<String, Object> data);

    /**
     * Save multiple entities
     */
    List<Map<String, Object>> saveAll(List<Map<String, Object>> dataList);

    /**
     * Delete entity by UUID
     */
    boolean deleteByUuid(UUID uuid);

    // ============ CUSTOM QUERY ============

    /**
     * Find by custom field (flexible querying)
     * Example: findByField("consultationUuid", someUuid)
     */
    List<Map<String, Object>> findByField(String fieldName, Object value);

    /**
     * Find by multiple fields
     * Example: findByFields(Map.of("patientUuid", uuid, "status", "ACTIVE"))
     */
    List<Map<String, Object>> findByFields(Map<String, Object> criteria);

    Optional <Object> findByObjectUuid(UUID uuid);
}