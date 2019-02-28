package org.iblog.enhance.gateway.db.mongo;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.core.OpenApi;
import org.iblog.enhance.gateway.db.BaseMongoDAO;
import org.iblog.enhance.gateway.db.filter.OpenApiFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author shaoxiao.xu
 * @date 2018/12/27 16:08
 */
@Repository
public class OpenApiMongoDAO extends BaseMongoDAO {
    private final String BUCKET = "open_apis";

    public OpenApiMongoDAO(final MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    public OpenApi create(OpenApi api) {
        mongoTemplate.insert(api, BUCKET);
        return api;
    }

    public OpenApi find(String id) {
        return mongoTemplate.findById(id, OpenApi.class);
    }

    public boolean exist(String id) {
        return mongoTemplate.exists(query(where("id").is(id)), OpenApi.class, BUCKET);
    }

    public OpenApi delete(String id) {
        return mongoTemplate.findAndRemove(
                query(where("id").is(id)), OpenApi.class, BUCKET);
    }

    public long update(OpenApi api) {
        return mongoTemplate.updateFirst(
                query(where("id").is(api.getId())),
                Update.update("uriPattern", api.getUriPattern())
                        .set("method", api.getMethod())
                        .set("apiCode", api.getApiCode())
                        .set("businessType", api.getBusinessType())
                        .set("filters", api.getFilters())
                        .set("createdAt", api.getCreatedAt())
                        .set("createdBy", api.getCreatedBy())
                        .set("lastUpdatedAt", api.getLastUpdatedAt())
                        .set("lastUpdatedBy", api.getLastUpdatedBy()),
                OpenApi.class, BUCKET)
                .getMatchedCount();
    }

    public boolean exist(OpenApiFilter filter) {
        Criteria criteria = bindCondition(filter);
        return mongoTemplate.exists(query(criteria), OpenApi.class, BUCKET);
    }

    /**
     * query by {@link Pageable}
     * @param filter
     * @return
     */
    public List<OpenApi> list(OpenApiFilter filter) {
        int pageSize = filter.to - filter.from;
        int page = filter.from / pageSize + 1;
        Criteria criteria = bindCondition(filter);
        Pageable pageable = PageRequest.of(
                page - 1, pageSize, new Sort(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(
                query(criteria).with(pageable),
                OpenApi.class, BUCKET);
    }

    public long count(OpenApiFilter filter) {
        Criteria criteria = bindCondition(filter);
        return mongoTemplate.count(query(criteria), OpenApi.class, BUCKET);
    }

    private Criteria bindCondition(OpenApiFilter filter) {
        Criteria criteria = where("createdAt").gte(filter.start)
                .andOperator(where("createdAt").lt(filter.end));
        if (CollectionUtils.isNotEmpty(filter.uris)) {
            criteria.and("uriPattern").in(filter.uris);
        }
        if (CollectionUtils.isNotEmpty(filter.methods)) {
            criteria.and("method").in(filter.methods);
        }
        if (CollectionUtils.isNotEmpty(filter.codes)) {
            criteria.and("apiCode").in(filter.codes);
        }
        if (CollectionUtils.isNotEmpty(filter.types)) {
            criteria.and("businessType").in(filter.types);
        }
        return criteria;
    }
}
