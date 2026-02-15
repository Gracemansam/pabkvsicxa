package com.lamiplus_common_api.api;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


@Slf4j
public abstract class AbstractPluginDataService implements PluginDataService {

    protected final PluginDataServiceRegistry dataServiceRegistry;

    protected abstract String getPluginId();

    @Override
    public abstract String getEntityName();

    protected AbstractPluginDataService(PluginDataServiceRegistry dataServiceRegistry) {
        this.dataServiceRegistry = dataServiceRegistry;
    }


    @PostConstruct
    public void register() {
        if (dataServiceRegistry != null) {
            dataServiceRegistry.register(getPluginId(), getEntityName(), this);
            log.info("Registered [{}/{}] → {}", getPluginId(), getEntityName(), getClass().getSimpleName());
        } else {
            log.warn("Registry unavailable, could not register [{}/{}]",
                    getPluginId(), getEntityName());
        }
    }


    @Override
    public Optional<Map<String, Object>> findByUuid(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public List<Map<String, Object>> findByUuids(List<UUID> uuids) {
        // Default: call findByUuid for each — override for batch optimization
        if (uuids == null || uuids.isEmpty()) return Collections.emptyList();
        List<Map<String, Object>> results = new ArrayList<>();
        for (UUID uuid : uuids) {
            findByUuid(uuid).ifPresent(results::add);
        }
        return results;
    }

    @Override
    public List<Map<String, Object>> findByPatientUuid(UUID patientUuid) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> findByTenantId(String tenantId) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> findByPatientUuidAndTenantId(UUID patientUuid, String tenantId) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> findAll(String tenantId, int page, int size) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> save(Map<String, Object> data) {
        throw new UnsupportedOperationException(
                "save() not implemented for " + getEntityName() + " in " + getClass().getSimpleName());
    }

    @Override
    public List<Map<String, Object>> saveAll(List<Map<String, Object>> dataList) {
        // Default: save one by one — override for batch optimization
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            results.add(save(data));
        }
        return results;
    }

    @Override
    public boolean deleteByUuid(UUID uuid) {
        return false;
    }

    @Override
    public List<Map<String, Object>> findByField(String fieldName, Object value) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> findByFields(Map<String, Object> criteria) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Object> findByObjectUuid(UUID uuid) {
        return Optional.empty();
    }



    protected UUID toUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        String str = value.toString().trim();
        if (str.isEmpty()) return null;
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID: '{}'", value);
            return null;
        }
    }

    protected Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        String str = value.toString().trim();
        if (str.isEmpty()) return null;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            log.warn("Invalid integer: '{}'", value);
            return null;
        }
    }

    protected Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        String str = value.toString().trim();
        if (str.isEmpty()) return null;
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            log.warn("Invalid long: '{}'", value);
            return null;
        }
    }

    protected String toString(Object value) {
        return value != null ? value.toString() : null;
    }

    protected LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception e) {
            log.warn("Invalid date: '{}'", value);
            return null;
        }
    }

    protected LocalTime toLocalTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalTime) return (LocalTime) value;
        try {
            return LocalTime.parse(value.toString());
        } catch (Exception e) {
            log.warn("Invalid time: '{}'", value);
            return null;
        }
    }

    protected LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        try {
            return LocalDateTime.parse(value.toString());
        } catch (Exception e) {
            log.warn("Invalid datetime: '{}'", value);
            return null;
        }
    }
}