package org.iblog.enhance.gateway.service;

import org.iblog.enhance.gateway.core.OpenApi;
import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.db.filter.OpenApiFilter;
import com.google.common.base.Optional;
import com.google.common.cache.CacheStats;

/**
 * @author shaoxiao.xu
 * @date 2018/12/28 18:07
 */
public interface OpenApiService {
    Optional<OpenApi> create(OpenApi api);
    boolean exist(String id);
    boolean exist(String uriPattern, String method);
    Optional<OpenApi> find(String id);
    Optional<OpenApi> find(String uriPattern, String method);
    Optional<OpenApi> update(OpenApi api);
    Optional<OpenApi> delete(String id);
    PageResult.Builder list(OpenApiFilter filter);
    CacheStats cacheStats();
}
