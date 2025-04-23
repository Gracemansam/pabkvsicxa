package com.lamiplus_common_api.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginPermission {

    private String pluginId;

    private String name;


    private String endpoint;



    private String actionType;


    private String resourceType;
    private String permissionCode;



    private String permissionExpression;


    private String description;
}