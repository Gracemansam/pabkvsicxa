package com.lamiplus_common_api.api;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.*;
import java.util.*;

/**
 * Auto-maps JPA entities to Map<String, Object> and back using reflection.
 * Eliminates manual toMap() / toEntity() methods in DataService classes.
 *
 * PROBLEM IT SOLVES:
 *   Developer adds a new field to Diagnosis entity but forgets to update toMap()
 *   → field silently disappears during inter-plugin communication.
 *
 * USAGE IN DATA SERVICES:
 *
 *   // Instead of writing a manual toMap():
 *   private Map<String, Object> toMap(Diagnosis entity) {
 *       return EntityMapper.toMap(entity);
 *   }
 *
 *   // Instead of writing a manual toEntity():
 *   private Diagnosis toEntity(Map<String, Object> data) {
 *       return EntityMapper.toEntity(data, Diagnosis.class);
 *   }
 *
 *   // Or with custom field handling for special cases (like JPA relationships):
 *   private Diagnosis toEntity(Map<String, Object> data) {
 *       Diagnosis d = EntityMapper.toEntity(data, Diagnosis.class);
 *       // Handle relationship manually — only the special case
 *       if (data.get("consultationUuid") != null) {
 *           d.setConsultation(consultationRepo.findByUuid(...).orElseThrow());
 *       }
 *       return d;
 *   }
 *
 * WHAT IT HANDLES:
 *   - UUID fields → serialized as String in map, deserialized back to UUID
 *   - Enum fields → serialized as String (.name()), deserialized back to enum
 *   - LocalDate, LocalTime, LocalDateTime → serialized as String, parsed back
 *   - Primitives, Strings, Numbers → passed through directly
 *   - Nested JPA entities → skipped (handle relationships manually)
 *   - Null values → included in map as null
 *
 * WHAT IT SKIPS:
 *   - Fields annotated with @Transient
 *   - Static and final fields
 *   - Collections and complex nested objects (log a warning)
 */
@Slf4j
public final class EntityMapper {

    private EntityMapper() {} // utility class

    // ========================
    // ENTITY → MAP
    // ========================

    /**
     * Convert any entity to Map<String, Object>.
     * Reads all declared fields (including inherited) via reflection.
     * UUIDs and Enums are serialized to Strings for safe transport.
     */
    public static Map<String, Object> toMap(Object entity) {
        if (entity == null) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        List<Field> fields = getAllFields(entity.getClass());

        for (Field field : fields) {
            if (shouldSkipField(field)) continue;

            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                String key = field.getName();
                map.put(key, serializeValue(value));
            } catch (IllegalAccessException e) {
                log.warn("Cannot access field '{}' on {}", field.getName(), entity.getClass().getSimpleName());
            }
        }

        return map;
    }

    /**
     * Convert entity to Map, but also flatten a specific JPA relationship
     * to just its UUID. Useful for ManyToOne relationships.
     *
     * Example:
     *   EntityMapper.toMap(diagnosis, Map.of("consultation", "consultationUuid"))
     *   → Instead of nesting the full Consultation object, puts "consultationUuid" → UUID string
     */
    public static Map<String, Object> toMap(Object entity, Map<String, String> relationshipToUuidField) {
        Map<String, Object> map = toMap(entity);

        for (Map.Entry<String, String> entry : relationshipToUuidField.entrySet()) {
            String relationField = entry.getKey();
            String uuidFieldName = entry.getValue();

            Object relatedEntity = map.remove(relationField);
            if (relatedEntity == null) {
                map.put(uuidFieldName, null);
                continue;
            }

            // Try to extract UUID from the related entity
            UUID uuid = extractUuid(relatedEntity);
            map.put(uuidFieldName, uuid != null ? uuid.toString() : null);
        }

        return map;
    }

    // ========================
    // MAP → ENTITY
    // ========================

    /**
     * Convert Map<String, Object> to an entity instance.
     * Matches map keys to field names and does type conversion.
     */
    public static <T> T toEntity(Map<String, Object> data, Class<T> entityClass) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert empty map to " + entityClass.getSimpleName());
        }

        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            List<Field> fields = getAllFields(entityClass);

            for (Field field : fields) {
                if (shouldSkipField(field)) continue;

                String key = field.getName();
                if (!data.containsKey(key)) continue;

                Object value = data.get(key);
                if (value == null) continue;

                field.setAccessible(true);
                Object converted = deserializeValue(value, field.getType());
                if (converted != null) {
                    field.set(entity, converted);
                }
            }

            return entity;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create entity " + entityClass.getSimpleName(), e);
        }
    }

    // ========================
    // SERIALIZATION (Entity field → Map value)
    // ========================

    private static Object serializeValue(Object value) {
        if (value == null) return null;

        // UUID → String
        if (value instanceof UUID) return value.toString();

        // Enum → String name
        if (value instanceof Enum<?>) return ((Enum<?>) value).name();

        // Date/Time → String (ISO format)
        if (value instanceof LocalDate || value instanceof LocalTime
                || value instanceof LocalDateTime) {
            return value.toString();
        }

        // Primitives and Strings pass through
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }

        // JPA entities / complex objects — try to extract UUID, otherwise skip
        if (isJpaEntity(value)) {
            UUID uuid = extractUuid(value);
            return uuid != null ? uuid.toString() : null;
        }

        // Collections — skip with warning
        if (value instanceof Collection || value instanceof Map) {
            return null;
        }

        return value.toString();
    }

    // ========================
    // DESERIALIZATION (Map value → Entity field)
    // ========================

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object deserializeValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        // Already the correct type
        if (targetType.isInstance(value)) return value;

        String strValue = value.toString().trim();
        if (strValue.isEmpty()) return null;

        try {
            // UUID
            if (targetType == UUID.class) return UUID.fromString(strValue);

            // String
            if (targetType == String.class) return strValue;

            // Integer / int
            if (targetType == Integer.class || targetType == int.class) {
                return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(strValue);
            }

            // Long / long
            if (targetType == Long.class || targetType == long.class) {
                return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(strValue);
            }

            // Double / double
            if (targetType == Double.class || targetType == double.class) {
                return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(strValue);
            }

            // Boolean / boolean
            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(strValue);
            }

            // LocalDate
            if (targetType == LocalDate.class) return LocalDate.parse(strValue);

            // LocalTime
            if (targetType == LocalTime.class) return LocalTime.parse(strValue);

            // LocalDateTime
            if (targetType == LocalDateTime.class) return LocalDateTime.parse(strValue);

            // Enum
            if (targetType.isEnum()) {
                return Enum.valueOf((Class<Enum>) targetType, strValue.toUpperCase());
            }

        } catch (Exception e) {
            log.warn("Failed to convert '{}' to {}: {}", value, targetType.getSimpleName(), e.getMessage());
            return null;
        }

        log.debug("Skipping unsupported type conversion: {} → {}", value.getClass().getSimpleName(), targetType.getSimpleName());
        return null;
    }

    // ========================
    // REFLECTION HELPERS
    // ========================

    /**
     * Get all fields from the class and its superclasses.
     * This picks up BaseAudit fields like tenantId, uuid, createdAt, etc.
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * Skip static, final, and @Transient fields.
     */
    private static boolean shouldSkipField(Field field) {
        int modifiers = field.getModifiers();
        if (java.lang.reflect.Modifier.isStatic(modifiers)) return true;
        if (java.lang.reflect.Modifier.isFinal(modifiers)) return true;

        // Skip JPA @Transient
        for (var annotation : field.getAnnotations()) {
            String name = annotation.annotationType().getSimpleName();
            if ("Transient".equals(name)) return true;
        }

        return false;
    }

    /**
     * Check if an object looks like a JPA entity (has @Entity or @Id annotation).
     */
    private static boolean isJpaEntity(Object value) {
        for (var annotation : value.getClass().getAnnotations()) {
            String name = annotation.annotationType().getSimpleName();
            if ("Entity".equals(name) || "MappedSuperclass".equals(name)) return true;
        }
        return false;
    }

    /**
     * Extract UUID from an entity by looking for a field named "uuid" or a getUuid() method.
     */
    private static UUID extractUuid(Object entity) {
        if (entity == null) return null;

        // Try field named "uuid"
        try {
            Field uuidField = findField(entity.getClass(), "uuid");
            if (uuidField != null) {
                uuidField.setAccessible(true);
                Object val = uuidField.get(entity);
                if (val instanceof UUID) return (UUID) val;
                if (val != null) return UUID.fromString(val.toString());
            }
        } catch (Exception ignored) {}

        // Try getUuid() method
        try {
            var method = entity.getClass().getMethod("getUuid");
            Object val = method.invoke(entity);
            if (val instanceof UUID) return (UUID) val;
            if (val != null) return UUID.fromString(val.toString());
        } catch (Exception ignored) {}

        return null;
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}