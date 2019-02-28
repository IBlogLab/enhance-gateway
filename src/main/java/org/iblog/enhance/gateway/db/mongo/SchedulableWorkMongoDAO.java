package org.iblog.enhance.gateway.db.mongo;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.core.SchedulableWork;
import org.iblog.enhance.gateway.db.BaseMongoDAO;
import org.iblog.enhance.gateway.db.filter.SchedulableWorkFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author lance
 */
@Repository
public class SchedulableWorkMongoDAO extends BaseMongoDAO {
    private final String BUCKET = "schedulable_works";

    public SchedulableWorkMongoDAO(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    public SchedulableWork create(SchedulableWork work) {
        mongoTemplate.insert(work, BUCKET);
        return work;
    }

    public SchedulableWork find(String recordId) {
        return mongoTemplate.findById(recordId, SchedulableWork.class);
    }

    public boolean exist(String recordId) {
        return mongoTemplate.exists(query(where("id").is(recordId)), SchedulableWork.class, BUCKET);
    }

    public SchedulableWork delete(String recordId) {
        return mongoTemplate.findAndRemove(
                query(where("id").is(recordId)), SchedulableWork.class, BUCKET);
    }

    public long update(SchedulableWork work) {
        return mongoTemplate.updateFirst(
                query(where("id").is(work.getId())),
                Update.update("maxRetries", work.getMaxRetries())
                        .set("retry", work.getRetry())
                        .set("intervalTime", work.getIntervalTime())
                        .set("scheduledAt", work.getScheduledAt())
                        .set("finished", work.isFinished())
                        .set("discarded", work.isDiscarded())
                        .set("type", work.getType())
                        .set("possessor", work.getPossessor()),
                SchedulableWork.class, BUCKET)
                .getMatchedCount();
    }

    public boolean exist(SchedulableWorkFilter filter) {
        Criteria criteria = bindCondition(filter);
        return mongoTemplate.exists(query(criteria), SchedulableWork.class, BUCKET);
    }

    /**
     * query by {@link Pageable}
     * @param filter
     * @return
     */
    public List<SchedulableWork> list(SchedulableWorkFilter filter) {
        int pageSize = filter.to - filter.from;
        int page = filter.from / pageSize + 1;
        Criteria criteria = bindCondition(filter);
        Pageable pageable = PageRequest.of(
                page - 1, pageSize, new Sort(Sort.Direction.ASC, "scheduledAt"));
        return mongoTemplate.find(
                query(criteria).with(pageable),
                SchedulableWork.class, BUCKET);
    }

    public SchedulableWork findAndModify(SchedulableWorkFilter filter, String possessor) {
        int pageSize = filter.to - filter.from;
        int page = filter.from / pageSize + 1;
        Pageable pageable = PageRequest.of(
                page - 1, pageSize, new Sort(Sort.Direction.ASC, "scheduledAt"));
        Criteria criteria = bindCondition(filter);
        FindAndModifyOptions upsert = new FindAndModifyOptions().returnNew(true).upsert(false);
        return mongoTemplate.findAndModify(
                query(criteria).with(pageable),
                Update.update("possessor", possessor),
                upsert,
                SchedulableWork.class,
                BUCKET);
    }

    public long count(SchedulableWorkFilter filter) {
        Criteria criteria = bindCondition(filter);
        return mongoTemplate.count(query(criteria), SchedulableWork.class, BUCKET);
    }

    private Criteria bindCondition(SchedulableWorkFilter filter) {
        Criteria criteria = where("scheduledAt").gte(filter.start)
                .andOperator(where("scheduledAt").lt(filter.end));
        if (filter.excludeFinished) {
            criteria.and("finished").is(false);
        }
        if (filter.uncredited) {
            criteria.and("possessor").is(null);
        }
        if (filter.excludeDiscarded) {
            criteria.and("discarded").is(false);
        }
        if (CollectionUtils.isNotEmpty(filter.types)) {
            criteria.and("type").in(filter.types);
        }
        return criteria;
    }
}
