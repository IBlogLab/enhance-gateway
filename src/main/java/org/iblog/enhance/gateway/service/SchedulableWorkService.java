package org.iblog.enhance.gateway.service;

import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.core.SchedulableWork;
import org.iblog.enhance.gateway.db.filter.SchedulableWorkFilter;
import com.google.common.base.Optional;

/**
 * @author shaoxiao.xu
 * @date 2019/2/21 10:52
 */
public interface SchedulableWorkService {
    Optional<SchedulableWork> create(SchedulableWork work);
    boolean exist(String recordId);
    Optional<SchedulableWork> find(String recordId);
    Optional<SchedulableWork> findAndModify(SchedulableWorkFilter filter, String possessor);
    Optional<SchedulableWork> update(SchedulableWork work);
    Optional<SchedulableWork> delete(String recordId);
    PageResult.Builder list(SchedulableWorkFilter filter);
}
