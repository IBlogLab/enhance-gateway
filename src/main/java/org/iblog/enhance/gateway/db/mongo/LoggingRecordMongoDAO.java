package org.iblog.enhance.gateway.db.mongo;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.db.BaseMongoDAO;
import org.iblog.enhance.gateway.db.filter.LoggingRecordFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.client.result.DeleteResult;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author shaoxiao.xu
 * @date 2018/12/29 17:06
 */
@Repository
public class LoggingRecordMongoDAO extends BaseMongoDAO {
    private final String BUCKET = "logging_records";

    public LoggingRecordMongoDAO(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    public LoggingRecord create(LoggingRecord record) {
        mongoTemplate.insert(record, BUCKET);
        return record;
    }

    public LoggingRecord find(String id) {
        return mongoTemplate.findById(id, LoggingRecord.class, BUCKET);
    }

    public boolean exist(String id) {
        return mongoTemplate.exists(query(where("id").is(id)), LoggingRecord.class, BUCKET);
    }

    public LoggingRecord delete(String id) {
        return mongoTemplate.findAndRemove(
                query(where("id").is(id)), LoggingRecord.class, BUCKET);
    }

    public long delete(List<String> ids) {
        DeleteResult result = mongoTemplate.remove(
                query(where("id").in(ids)), LoggingRecord.class, BUCKET);
        return result.getDeletedCount();
    }

    public long updateIf(LoggingRecord record) {
        Update update = new Update();
        if (!Strings.isNullOrEmpty(record.getUri())) {
            update.set("uri", record.getUri());
        }
        if (!Strings.isNullOrEmpty(record.getInterrelatedId())) {
            update.set("interrelatedId", record.getInterrelatedId());
        }
        if (!Strings.isNullOrEmpty(record.getApiCode())) {
            update.set("apiCode", record.getApiCode());
        }
        if (!Strings.isNullOrEmpty(record.getBusinessType())) {
            update.set("businessType", record.getBusinessType());
        }
        if (!Strings.isNullOrEmpty(record.getClientId())) {
            update.set("clientId", record.getClientId());
        }
        if (!Strings.isNullOrEmpty(record.getSecretKey())) {
            update.set("secretKey", record.getSecretKey());
        }
        if (!Strings.isNullOrEmpty(record.getWarehouse())) {
            update.set("warehouse", record.getWarehouse());
        }
        if (!Strings.isNullOrEmpty(record.getFrom())) {
            update.set("from", record.getFrom());
        }
        if (!Strings.isNullOrEmpty(record.getTo())) {
            update.set("to", record.getTo());
        }
        if (record.isExternalized()) {
            update.set("externalized", record.isExternalized());
        }
        if (record.isAsync()) {
            update.set("async", record.isAsync());
        }
        if (record.getError() != null) {
            update.set("error", record.getError());
        }
        if (CollectionUtils.isNotEmpty(record.getTags())) {
            update.set("tags", record.getTags());
        }
        if (!Strings.isNullOrEmpty(record.getIp())) {
            update.set("ip", record.getIp());
        }
        if (!Strings.isNullOrEmpty(record.getUrl())) {
            update.set("url", record.getUrl());
        }
        if (!Strings.isNullOrEmpty(record.getRealUrl())) {
            update.set("realUrl", record.getRealUrl());
        }
        if (record.getMethod() != null) {
            update.set("method", record.getMethod());
        }
        if (record.getRequestType() != null) {
            update.set("requestType", record.getRequestType());
        }
        if (!Strings.isNullOrEmpty(record.getRequestHeaders())) {
            update.set("requestHeaders", record.getRequestHeaders());
        }
        if (!Strings.isNullOrEmpty(record.getResponseHeaders())) {
            update.set("responseHeaders", record.getResponseHeaders());
        }
        if (!Strings.isNullOrEmpty(record.getQueries())) {
            update.set("queries", record.getQueries());
        }
        if (record.getStatusCode() > 0) {
            update.set("statusCode", record.getStatusCode());
        }
        if (!Strings.isNullOrEmpty(record.getRequestBody())) {
            update.set("requestBody", record.getRequestBody());
        }
        if (!Strings.isNullOrEmpty(record.getRequestConvertedBody())) {
            update.set("requestConvertedBody", record.getRequestConvertedBody());
        }
        if (!Strings.isNullOrEmpty(record.getResponseBody())) {
            update.set("responseBody", record.getResponseBody());
        }
        if (!Strings.isNullOrEmpty(record.getResponseConvertedBody())) {
            update.set("responseConvertedBody", record.getResponseConvertedBody());
        }
        if (record.getStartTime() > 0) {
            update.set("startTime", record.getStartTime());
        }
        if (record.getEndTime() > 0) {
            update.set("endTime", record.getEndTime());
        }
        if (record.getCreatedAt() > 0) {
            update.set("createdAt", record.getCreatedAt());
        }
        if (record.getLastUpdatedAt() > 0) {
            update.set("lastUpdatedAt", record.getLastUpdatedAt());
        }
        if (!Strings.isNullOrEmpty(record.getCreatedBy())) {
            update.set("createdBy", record.getCreatedBy());
        }
        if (!Strings.isNullOrEmpty(record.getLastUpdatedBy())) {
            update.set("lastUpdatedBy", record.getLastUpdatedBy());
        }
        return mongoTemplate.updateFirst(query(where("id").is(record.getId())), update,
                LoggingRecord.class, BUCKET).getModifiedCount();
    }

    public long update(LoggingRecord record) {
        return mongoTemplate.updateFirst(
                query(where("id").is(record.getId())),
                Update.update("uri", record.getUri())
                        .set("interrelatedId", record.getInterrelatedId())
                        .set("apiCode", record.getApiCode())
                        .set("businessType", record.getBusinessType())
                        .set("clientId", record.getClientId())
                        .set("secretKey", record.getSecretKey())
                        .set("warehouse", record.getWarehouse())
                        .set("from", record.getFrom())
                        .set("to", record.getTo())
                        .set("externalized", record.isExternalized())
                        .set("async", record.isAsync())
                        .set("error", record.getError())
                        .set("tags", record.getTags())
                        .set("ip", record.getIp())
                        .set("url", record.getUrl())
                        .set("realUrl", record.getRealUrl())
                        .set("method", record.getMethod())
                        .set("requestType", record.getRequestType())
                        .set("requestHeaders", record.getRequestHeaders())
                        .set("responseHeaders", record.getResponseHeaders())
                        .set("queries", record.getQueries())
                        .set("statusCode", record.getStatusCode())
                        .set("requestBody", record.getRequestBody())
                        .set("requestConvertedBody", record.getRequestConvertedBody())
                        .set("responseBody", record.getResponseBody())
                        .set("responseConvertedBody", record.getResponseConvertedBody())
                        .set("startTime", record.getStartTime())
                        .set("endTime", record.getEndTime())
                        .set("createdAt", record.getCreatedAt())
                        .set("lastUpdatedAt", record.getLastUpdatedAt())
                        .set("createdBy", record.getCreatedBy())
                        .set("lastUpdatedBy", record.getLastUpdatedBy()),
                LoggingRecord.class, BUCKET)
                .getMatchedCount();
    }

    public boolean exist(LoggingRecordFilter filter) {
        Criteria criteria = bindCondition(filter);
        return mongoTemplate.exists(query(criteria), LoggingRecord.class, BUCKET);
    }

    /**
     * query by {@link Pageable}
     * @param filter
     * @return
     */
    public List<LoggingRecord> list(LoggingRecordFilter filter) {
        int pageSize = filter.to - filter.from;
        int page = filter.from / pageSize + 1;
        Criteria criteria = bindCondition(filter);
        Pageable pageable = PageRequest.of(
                page - 1, pageSize, new Sort(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(
                query(criteria).with(pageable),
                LoggingRecord.class, BUCKET);
    }

    public long count(LoggingRecordFilter filter) {
        Criteria criteria = bindCondition(filter);
        return mongoTemplate.count(query(criteria), LoggingRecord.class, BUCKET);
    }

    private Criteria bindCondition(LoggingRecordFilter filter) {
        Criteria criteria = new Criteria();
        List<Criteria> result = Lists.newArrayList();
        result.add(where("createdAt").gte(filter.start).lt(filter.end));
        result.add(where("startTime").gte(filter.reqRange[0]).lt(filter.reqRange[1]));
        result.add(where("endTime").gte(filter.respRange[0]).lt(filter.respRange[1]));
        if (CollectionUtils.isNotEmpty(filter.uris)) {
            Criteria tmp = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>(filter.uris.size());
            for (int i = 0; i < filter.uris.size(); i++) {
                criteriaList.add(where("uri").regex(filter.uris.get(i)));
            }
            tmp.orOperator(criteriaList.toArray(new Criteria[0]));
            result.add(tmp);
        }
        if (CollectionUtils.isNotEmpty(filter.types)) {
            result.add(where("businessType").in(filter.types));
        }
        if (filter.excludeAsync && !filter.excludeSync) {
            result.add(where("async").is(true));
        }
        if (filter.excludeSync && !filter.excludeAsync) {
            result.add(where("async").is(false));
        }
        if (CollectionUtils.isNotEmpty(filter.errors)) {
            result.add(where("error").in(filter.errors));
        }
        if (CollectionUtils.isNotEmpty(filter.methods)) {
            result.add(where("method").in(filter.methods));
        }
        if (filter.excludeHandleSuccess && !filter.excludeHandleFail) {
            result.add(where("statusCode").nin(filter.statuses));
        } else if (!filter.excludeHandleSuccess && filter.excludeHandleFail) {
            result.add(where("statusCode").in(filter.statuses));
        } else if (CollectionUtils.isNotEmpty(filter.statuses)) {
            result.add(where("statusCode").in(filter.statuses));
        }
        if (CollectionUtils.isNotEmpty(filter.froms)) {
            result.add(where("from").in(filter.froms));
        }
        if (CollectionUtils.isNotEmpty(filter.tos)) {
            result.add(where("to").in(filter.tos));
        }
        if (CollectionUtils.isNotEmpty(filter.tags)) {
            Criteria tmp = new Criteria();
            // TODO how to construct keyword query.
            List<Criteria> criteriaList = Lists.newArrayList();
            for (int i = 0; i < filter.tags.size(); i++) {
                Criteria key = where("tags.key").is(filter.tags.get(i).getKey());
                for (int j = 0; j < filter.tags.get(i).getValues().size(); j++) {
                    key.and("tags.values").regex(filter.tags.get(i).getValues().get(j), "");
                    criteriaList.add(key);
                }
            }
            tmp.orOperator(criteriaList.toArray(new Criteria[0]));
            result.add(tmp);
        }
        return criteria.andOperator(result.toArray(new Criteria[0]));
    }
}
