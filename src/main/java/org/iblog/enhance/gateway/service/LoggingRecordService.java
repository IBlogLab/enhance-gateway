package org.iblog.enhance.gateway.service;

import java.util.List;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.db.filter.LoggingRecordFilter;
import com.google.common.base.Optional;

/**
 * @author shaoxiao.xu
 * @date 2019/1/3 13:57
 */
public interface LoggingRecordService {
    Optional<LoggingRecord> create(LoggingRecord record);
    boolean exist(String id);
    boolean exist(LoggingRecordFilter filter);
    Optional<LoggingRecord> find(String id);
    Optional<LoggingRecord> update(LoggingRecord record);
    Optional<LoggingRecord> updateIf(LoggingRecord record);
    Optional<LoggingRecord> delete(String id);
    long delete(List<String> ids);
    PageResult.Builder list(LoggingRecordFilter filter);
}
