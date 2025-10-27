package com.lamiplus_common_api.api;

import java.util.List;
import java.util.Optional;


public interface PluginCommunicationService {


    Optional<Plugin> getPlugin(String pluginId);


    <T extends Plugin> Optional<T> getPlugin(Class<T> pluginClass, String pluginId);


    boolean isPluginAvailable(String pluginId);


    List<Plugin> getAllPlugins();


    <T> Optional<T> findService(Class<T> serviceClass);


    <T> Optional<T> findService(String pluginId, Class<T> serviceClass);


    <T> List<T> findAllServices(Class<T> serviceClass);


    boolean hasService(Class<?> serviceClass);


    Optional<ServiceProxy> getServiceProxy(String pluginId, String serviceClassName);
}