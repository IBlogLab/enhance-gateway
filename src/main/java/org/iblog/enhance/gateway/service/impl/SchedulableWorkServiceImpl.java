package org.iblog.enhance.gateway.service.impl;

import java.util.List;
import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.core.SchedulableWork;
import org.iblog.enhance.gateway.db.filter.SchedulableWorkFilter;
import org.iblog.enhance.gateway.db.mongo.SchedulableWorkMongoDAO;
import org.iblog.enhance.gateway.service.SchedulableWorkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

/**
 * @author lance
 */
@Service
public class SchedulableWorkServiceImpl implements SchedulableWorkService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulableWorkServiceImpl.class);

    private SchedulableWorkMongoDAO mongoDAO;

    @Autowired
    public SchedulableWorkServiceImpl(final SchedulableWorkMongoDAO mongoDAO) {
        this.mongoDAO = mongoDAO;
    }

    @Override
    public Optional<SchedulableWork> create(SchedulableWork work) {
        if (Strings.isNullOrEmpty(work.getId()) || Strings.isNullOrEmpty(work.getType())) {
            return Optional.absent();
        }
        if (mongoDAO.exist(work.getId())) {
            return Optional.absent();
        }
        return Optional.of(mongoDAO.create(work));
    }

    @Override
    public boolean exist(String recordId) {
        return mongoDAO.exist(recordId);
    }

    @Override
    public Optional<SchedulableWork> find(String recordId) {
        if (Strings.isNullOrEmpty(recordId)) {
            return Optional.absent();
        }
        SchedulableWork exist = mongoDAO.find(recordId);
        return exist == null ? Optional.absent() : Optional.of(exist);
    }

    @Override
    public Optional<SchedulableWork> findAndModify(SchedulableWorkFilter filter, String possessor) {
        SchedulableWork exist = mongoDAO.findAndModify(filter, possessor);
        return exist == null ? Optional.absent() : Optional.of(exist);
    }

    @Override
    public Optional<SchedulableWork> update(SchedulableWork work) {
        if (Strings.isNullOrEmpty(work.getId()) || Strings.isNullOrEmpty(work.getType())) {
            return Optional.absent();
        }
        long updated = mongoDAO.update(work);
        return updated == 0 ? Optional.absent() : Optional.of(work);
    }

    @Override
    public Optional<SchedulableWork> delete(String recordId) {
        SchedulableWork deleted = mongoDAO.delete(recordId);
        return deleted == null ? Optional.absent() : Optional.of(deleted);
    }

    @Override
    public PageResult.Builder list(SchedulableWorkFilter filter) {
        PageResult.Builder builder = new PageResult.Builder();
        long total = mongoDAO.count(filter);
        builder.setPageInfo(filter.from, filter.to, total);
        List<SchedulableWork> works = mongoDAO.list(filter);
        builder.setData(works);
        return builder;
    }
}
