package com.lamiplus_common_api.api;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Optional;


@Slf4j
public class ServiceProxy {

    private final Object service;
    private final String serviceName;

    public ServiceProxy(Object service, String serviceName) {
        this.service = service;
        this.serviceName = serviceName;
    }


    @SuppressWarnings("unchecked")
    public <T> Optional<T> callOptional(String methodName, Object... params) {
        try {
            Method method = findMethod(methodName, params);
            if (method == null) {
                log.warn("Method {} not found in service {}", methodName, serviceName);
                return Optional.empty();
            }

            Object result = method.invoke(service, params);
            return (Optional<T>) result;
        } catch (Exception e) {
            log.error("Error calling {}.{}: {}", serviceName, methodName, e.getMessage(), e);
            return Optional.empty();
        }
    }


    public boolean callBoolean(String methodName, Object... params) {
        try {
            Method method = findMethod(methodName, params);
            if (method == null) {
                log.warn("Method {} not found in service {}", methodName, serviceName);
                return false;
            }

            Object result = method.invoke(service, params);
            return result != null && (Boolean) result;
        } catch (Exception e) {
            log.error("Error calling {}.{}: {}", serviceName, methodName, e.getMessage(), e);
            return false;
        }
    }


    public <T> T call(String methodName, Class<T> returnType, Object... params) {
        try {
            Method method = findMethod(methodName, params);
            if (method == null) {
                log.warn("Method {} not found in service {}", methodName, serviceName);
                return null;
            }

            Object result = method.invoke(service, params);
            return result != null ? returnType.cast(result) : null;
        } catch (Exception e) {
            log.error("Error calling {}.{}: {}", serviceName, methodName, e.getMessage(), e);
            return null;
        }
    }

    public void callVoid(String methodName, Object... params) {
        try {
            Method method = findMethod(methodName, params);
            if (method == null) {
                log.warn("Method {} not found in service {}", methodName, serviceName);
                return;
            }

            method.invoke(service, params);
        } catch (Exception e) {
            log.error("Error calling {}.{}: {}", serviceName, methodName, e.getMessage(), e);
        }
    }


    public long callLong(String methodName, Object... params) {
        try {
            Method method = findMethod(methodName, params);
            if (method == null) {
                log.warn("Method {} not found in service {}", methodName, serviceName);
                return 0L;
            }

            Object result = method.invoke(service, params);
            return result != null ? (Long) result : 0L;
        } catch (Exception e) {
            log.error("Error calling {}.{}: {}", serviceName, methodName, e.getMessage(), e);
            return 0L;
        }
    }


    public int callInt(String methodName, Object... params) {
        try {
            Method method = findMethod(methodName, params);
            if (method == null) {
                log.warn("Method {} not found in service {}", methodName, serviceName);
                return 0;
            }

            Object result = method.invoke(service, params);
            return result != null ? (Integer) result : 0;
        } catch (Exception e) {
            log.error("Error calling {}.{}: {}", serviceName, methodName, e.getMessage(), e);
            return 0;
        }
    }


    private Method findMethod(String methodName, Object... params) {
        try {
            // Build parameter types array
            Class<?>[] paramTypes = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                if (params[i] != null) {
                    paramTypes[i] = params[i].getClass();
                    // Handle primitive wrapper to primitive conversion
                    paramTypes[i] = unwrapPrimitive(paramTypes[i]);
                } else {
                    paramTypes[i] = Object.class;
                }
            }

            // Try exact match first
            try {
                return service.getClass().getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                // Try to find by name and parameter count (handles primitive/wrapper mismatches)
                for (Method method : service.getClass().getMethods()) {
                    if (method.getName().equals(methodName) &&
                            method.getParameterCount() == params.length) {

                        // Check if parameters are compatible
                        Class<?>[] methodParamTypes = method.getParameterTypes();
                        boolean compatible = true;

                        for (int i = 0; i < params.length; i++) {
                            if (params[i] != null &&
                                    !isCompatible(methodParamTypes[i], params[i].getClass())) {
                                compatible = false;
                                break;
                            }
                        }

                        if (compatible) {
                            return method;
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error finding method {}: {}", methodName, e.getMessage());
            return null;
        }
    }


    private boolean isCompatible(Class<?> paramType, Class<?> valueType) {
        // Handle primitives and their wrappers
        paramType = unwrapPrimitive(paramType);
        valueType = unwrapPrimitive(valueType);

        return paramType.isAssignableFrom(valueType);
    }


    private Class<?> unwrapPrimitive(Class<?> type) {
        if (type == Integer.class) return int.class;
        if (type == Long.class) return long.class;
        if (type == Boolean.class) return boolean.class;
        if (type == Double.class) return double.class;
        if (type == Float.class) return float.class;
        if (type == Short.class) return short.class;
        if (type == Byte.class) return byte.class;
        if (type == Character.class) return char.class;
        return type;
    }


    public Object getUnderlyingService() {
        return service;
    }


    public String getServiceName() {
        return serviceName;
    }
}