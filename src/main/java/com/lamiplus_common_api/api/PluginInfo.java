package com.lamiplus_common_api.api;


import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@AllArgsConstructor
@NoArgsConstructor
public class PluginInfo {

    private String pluginId;


    private String name;

    private String version;

    private String description;
    private String author;

    private String frontendPath;

    private List<String> dependencies  = new ArrayList<>();

    private String jarFile;

    public String getFrontendPath() {
        return frontendPath;
    }

    public void setFrontendPath(String frontendPath) {
        this.frontendPath = frontendPath;
    }

    private PluginType pluginType = PluginType.SERVICE;
    private boolean autoLoad = false;
    private boolean loaded = false;



    public boolean isBase() {
        return pluginType == PluginType.BASE;
    }

    public boolean isService() {
        return pluginType == PluginType.SERVICE;
    }


    private List<Class<?>> entities = new ArrayList<>();

    private List<Class<?>> controllers = new ArrayList<>();

    private List<Class<?>> services = new ArrayList<>();

    private List<Class<?>> repositories = new ArrayList<>();


    private List<Class<?>> components = new ArrayList<>();

    private LocalDateTime installedAt = LocalDateTime.now();

    private PluginStatus status = PluginStatus.STOPPED;

    public boolean isActive() {
        return status == PluginStatus.ACTIVE;
    }

    public PluginType getPluginType() { return pluginType; }
    public void setPluginType(PluginType pluginType) { this.pluginType = pluginType; }

    public boolean isAutoLoad() { return autoLoad; }
    public void setAutoLoad(boolean autoLoad) { this.autoLoad = autoLoad; }

    public boolean isLoaded() { return loaded; }
    public void setLoaded(boolean loaded) { this.loaded = loaded; }

    public String getPluginId() {
        return pluginId;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public String getJarFile() {
        return jarFile;
    }

    public List<Class<?>> getEntities() {
        return entities;
    }

    public List<Class<?>> getControllers() {
        return controllers;
    }

    public List<Class<?>> getServices() {
        return services;
    }

    public List<Class<?>> getRepositories() {
        return repositories;
    }

    public List<Class<?>> getComponents() {
        return components;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public PluginStatus getStatus() {
        return status;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
    }

    public void setEntities(List<Class<?>> entities) {
        this.entities = entities;
    }

    public void setControllers(List<Class<?>> controllers) {
        this.controllers = controllers;
    }

    public void setServices(List<Class<?>> services) {
        this.services = services;
    }

    public void setRepositories(List<Class<?>> repositories) {
        this.repositories = repositories;
    }

    public void setComponents(List<Class<?>> components) {
        this.components = components;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public void setStatus(PluginStatus status) {
        this.status = status;
    }
}