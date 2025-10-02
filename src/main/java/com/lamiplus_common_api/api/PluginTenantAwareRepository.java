package com.lamiplus_common_api.api;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface PluginTenantAwareRepository<T, ID> extends JpaRepository<T, ID> {
    @Query("SELECT e FROM #{#entityName} e WHERE e.tenantId = ?#{T(coreapplication.service.plugin_manager.TenantContext).getTenantId()}")
    @Override
    List<T> findAll();
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = ?1 AND e.tenantId = ?#{T(coreapplication.service.plugin_manager.TenantContext).getTenantId()}")
    @Override
    Optional<T> findById(ID id);
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.tenantId = ?#{T(coreapplication.service.plugin_manager.TenantContext).getTenantId()}")
    @Override
    long count();
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = ?1 AND e.tenantId = ?#{T(coreapplication.service.plugin_manager.TenantContext).getTenantId()}")
    @Override
    boolean existsById(ID id);
    @Query("DELETE FROM #{#entityName} e WHERE e.id = ?1 AND e.tenantId = ?#{T(coreapplication.service.plugin_manager.TenantContext).getTenantId()}")
    @Override
    void deleteById(ID id);
}
