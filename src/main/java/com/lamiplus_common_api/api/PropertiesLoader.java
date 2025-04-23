package com.lamiplus_common_api.api;

import java.util.Optional;

public interface PropertiesLoader {
    Optional<PluginProperties> loadProperties();
}
