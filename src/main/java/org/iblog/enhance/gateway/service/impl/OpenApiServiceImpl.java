package org.iblog.enhance.gateway.service.impl;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.iblog.enhance.gateway.core.OpenApi;
import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.db.filter.OpenApiFilter;
import org.iblog.enhance.gateway.db.mongo.OpenApiMongoDAO;
import org.iblog.enhance.gateway.service.OpenApiService;
import org.iblog.enhance.gateway.util.Clock;
import org.iblog.enhance.gateway.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.RemovalListener;

/**
 * @author shaoxiao.xu
 * @date 2018/12/28 18:09
 */
@Service
public class OpenApiServiceImpl implements OpenApiService {
    private static final Logger logger = LoggerFactory.getLogger(OpenApiServiceImpl.class);

    private final Clock clock = Clock.defaultClock();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final OpenApiMongoDAO mongoDAO;

    @Autowired
    public OpenApiServiceImpl(final OpenApiMongoDAO mongoDAO) {
        this.mongoDAO = mongoDAO;
    }

    private final Cache<String, OpenApi> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .initialCapacity(10)
            .maximumSize(1000)
            .recordStats()
            .removalListener((RemovalListener<String, OpenApi>) notification ->
                    logger.info("cache: {} was removed that cause by {}",
                            notification.getKey(), notification.getCause()))
            .build(new CacheLoader<String, OpenApi>() {
                @Override
                public OpenApi load(String key) throws Exception {
                    return mongoDAO.find(key);
                }
            });

    @Override
    public Optional<OpenApi> create(OpenApi api) {
        String generateId = generateId(api.getUriPattern(), api.getMethod());
        if (exist(generateId)) {
            return Optional.absent();
        }
        api.setId(generateId);
        api.setCreatedAt(clock.getTime());
        api.setLastUpdatedAt(api.getCreatedAt());
        mongoDAO.create(api);
        return Optional.of(api);
    }

    @Override
    public boolean exist(String id) {
        OpenApi api = cache.getIfPresent(id);
        if (api != null) {
            return true;
        }
        return mongoDAO.exist(id);
    }

    @Override
    public boolean exist(String uriPattern, String method) {
        String key = generateId(uriPattern, method);
        OpenApi exist = cache.getIfPresent(key);
        if (exist != null) {
            return true;
        }
        exist = find(uriPattern, method).orNull();
        if (exist == null) {
            return false;
        }
        return true;
    }

    @Override
    public Optional<OpenApi> find(String id) {
        if (Strings.isNullOrEmpty(id)) {
            return Optional.absent();
        }
        OpenApi exist = cache.getIfPresent(id);
        if (exist != null) {
            return Optional.of(exist);
        }
        exist = mongoDAO.find(id);
        if (exist == null) {
            return Optional.absent();
        } else {
            cache.put(id, exist);
            return Optional.of(exist);
        }
    }

    @Override
    public Optional<OpenApi> find(String uriPattern, String method) {
        String key = generateId(uriPattern, method);
        OpenApi exist = cache.getIfPresent(key);
        if (exist != null) {
            return Optional.of(exist);
        }

        PageResult result = list(new OpenApiFilter.Builder().build()).build();
        if (result.getTotalCount() == 0) {
            return Optional.absent();
        }
        List<OpenApi> apis = (List<OpenApi>) result.getData();
        apis = apis.stream()
                .filter(api -> !Strings.isNullOrEmpty(api.getMethod())
                        && api.getMethod().equals(method))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(apis)) {
            return Optional.absent();
        }
        for (OpenApi api : apis) {
            if (!api.getMethod().equals(method)) {
                continue;
            }
            if (!api.isRegex() && !api.getUriPattern().equals(uriPattern)) {
                continue;
            }
            if (!api.isRegex() && api.getUriPattern().equals(uriPattern)) {
                cache.put(key, api);
                return Optional.of(api);
            }
            if (pathMatcher.match(api.getUriPattern(), uriPattern)) {
                cache.put(key, api);
                return Optional.of(api);
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<OpenApi> update(OpenApi api) {
        api.setLastUpdatedAt(clock.getTime());
        OpenApi cacheBefore = cache.getIfPresent(api.getId());
        if (cacheBefore != null) {
            cache.put(api.getId(), api);
        }
        long update = mongoDAO.update(api);
        if (update == 0) {
            cache.invalidate(api.getId());
            return Optional.absent();
        }
        return Optional.of(api);
    }

    @Override
    public Optional<OpenApi> delete(String id) {
        OpenApi delete = mongoDAO.delete(id);
        if (delete != null) {
            cache.invalidate(id);
            return Optional.of(delete);
        }
        return Optional.absent();
    }

    @Override
    public PageResult.Builder list(OpenApiFilter filter) {
        PageResult.Builder builder = new PageResult.Builder();
        long total = mongoDAO.count(filter);
        builder.setPageInfo(filter.from, filter.to, total);
        List<OpenApi> apis = mongoDAO.list(filter);
        if (CollectionUtils.isNotEmpty(apis)) {
            apis.forEach(api -> cache.put(api.getId(), api));
        }
        builder.setData(apis);
        return builder;
    }

    @Override
    public CacheStats cacheStats() {
        return cache.stats();
    }

    /**
     * produce a unique logic primary key
     * @param uriPattern check for not blank
     * @param method check for not blank
     * @return
     */
    private String generateId(@NotBlank String uriPattern, @NotBlank String method) {
        List<String> keys = new ArrayList<>(2);
        keys.add(uriPattern);
        keys.add(method);
        return SecurityUtil.md5sum(StringUtils.join(keys, "_"));
    }
}
