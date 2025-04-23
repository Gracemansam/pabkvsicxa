package com.lamiplus_common_api.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginProperties {
    private String id;
    private String name;
    private String description;
    private String version;
    private String author;
    private List<String> dependencies = Collections.emptyList();
    private List<String> requiredRoles = Collections.emptyList();
    private List<String> auditableActions = Collections.emptyList();
    private List<String> auditableResourceTypes = Collections.emptyList();
    private List<PermissionConfig> permissions = new ArrayList<>();


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies != null ?
                Collections.unmodifiableList(dependencies) : Collections.emptyList();
    }

    public List<String> getRequiredRoles() { return requiredRoles; }
    public void setRequiredRoles(List<String> requiredRoles) {
        this.requiredRoles = requiredRoles != null ?
                Collections.unmodifiableList(requiredRoles) : Collections.emptyList();
    }

    public List<String> getAuditableActions() { return auditableActions; }
    public void setAuditableActions(List<String> auditableActions) {
        this.auditableActions = auditableActions != null ?
                Collections.unmodifiableList(auditableActions) : Collections.emptyList();
    }

    public List<String> getAuditableResourceTypes() { return auditableResourceTypes; }
    public void setAuditableResourceTypes(List<String> auditableResourceTypes) {
        this.auditableResourceTypes = auditableResourceTypes != null ?
                Collections.unmodifiableList(auditableResourceTypes) : Collections.emptyList();
    }

    public List<PermissionConfig> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionConfig> permissions) {
        this.permissions = permissions != null ?
                Collections.unmodifiableList(permissions) : Collections.emptyList();
    }
}
