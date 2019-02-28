package org.iblog.enhance.gateway.service.impl;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.db.filter.LoggingRecordFilter;
import org.iblog.enhance.gateway.db.mongo.LoggingRecordMongoDAO;
import org.iblog.enhance.gateway.service.LoggingRecordService;
import org.iblog.enhance.gateway.util.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.base.Optional;

/**
 * @author lance
 */
@Service
public class LoggingRecordServiceImpl implements LoggingRecordService {
    @Autowired
    private LoggingRecordMongoDAO mongoDAO;
    private final Clock clock = Clock.defaultClock();

    @Override
    public Optional<LoggingRecord> create(LoggingRecord record) {
        if (record.getCreatedAt() == 0) {
            record.setCreatedAt(clock.getTime());
            record.setLastUpdatedAt(record.getCreatedAt());
        }
        return Optional.of(mongoDAO.create(record));
    }

    @Override
    public boolean exist(String id) {
        return mongoDAO.exist(id);
    }

    @Override
    public boolean exist(LoggingRecordFilter filter) {
        return mongoDAO.exist(filter);
    }

    @Override
    public Optional<LoggingRecord> find(String id) {
        LoggingRecord exist = mongoDAO.find(id);
        return exist == null ? Optional.absent() : Optional.of(exist);
    }

    @Override
    public Optional<LoggingRecord> update(LoggingRecord record) {
        record.setLastUpdatedAt(clock.getTime());
        return mongoDAO.update(record) == 0 ? Optional.absent() : Optional.of(record);
    }

    @Override
    public Optional<LoggingRecord> updateIf(LoggingRecord record) {
        record.setLastUpdatedAt(clock.getTime());
        return mongoDAO.updateIf(record) == 0 ? Optional.absent() : Optional.of(record);
    }

    @Override
    public Optional<LoggingRecord> delete(String id) {
        LoggingRecord delete = mongoDAO.delete(id);
        return delete == null ? Optional.absent() : Optional.of(delete);
    }

    @Override
    public long delete(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        return mongoDAO.delete(ids);
    }

    @Override
    public PageResult.Builder list(LoggingRecordFilter filter) {
        PageResult.Builder builder = new PageResult.Builder();
        long total = mongoDAO.count(filter);
        builder.setPageInfo(filter.from, filter.to, total);
        List<LoggingRecord> records = mongoDAO.list(filter);
        builder.setData(records);
        return builder;
    }
}
