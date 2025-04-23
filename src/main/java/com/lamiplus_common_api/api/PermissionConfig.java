package com.lamiplus_common_api.api;

public class PermissionConfig {
    private String name;
    private String code;
    private String actionType;
    private String resourceType;
    private String description;
    private String endpoint;
    private String expression;



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
}