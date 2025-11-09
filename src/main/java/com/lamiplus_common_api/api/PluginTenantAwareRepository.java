package com.lamiplus_common_api.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface PluginTenantAwareRepository<T, ID> extends JpaRepository<T, ID> {

}