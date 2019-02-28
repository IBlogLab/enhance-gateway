package org.iblog.enhance.gateway.mongo;

import java.util.List;
import org.iblog.enhance.gateway.core.SchedulableWork;
import org.iblog.enhance.gateway.db.filter.SchedulableWorkFilter;
import org.iblog.enhance.gateway.db.mongo.SchedulableWorkMongoDAO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SchedulableWorkMongoDAOTest extends MongodbDAOTestBase {
    private SchedulableWorkMongoDAO mongoDAO;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mongoDAO = new SchedulableWorkMongoDAO(mongoTemplate);
    }

    @Test
    public void findAndModify() {
        initData();
        final String possessor = "possessor_1";
        SchedulableWorkFilter filter = new SchedulableWorkFilter.Builder()
                .setExcludeFinished(true)
                .setUncredited(true)
                .build();
        List<SchedulableWork> list = mongoDAO.list(filter);
        assertEquals(1, list.size());
        SchedulableWork work = mongoDAO.findAndModify(filter, possessor);
        assertNotNull(work);
        assertEquals("work_1", work.getId());
        assertEquals(possessor, work.getPossessor());

        work.setFinished(true);
        mongoDAO.update(work);

        work = mongoDAO.findAndModify(filter, possessor);
        assertNotNull(work);
        assertEquals("work_2", work.getId());
        assertEquals(possessor, work.getPossessor());
    }

    private void initData() {
        {
            SchedulableWork work = new SchedulableWork();
            work.setId("work_1");
            work.setRetry(10);
            work.setMaxRetries(10);
            work.setScheduledAt(1000);
            work.setIntervalTime(100);
            work.setType("type_1");
            mongoDAO.create(work);
        }
        {
            SchedulableWork work = new SchedulableWork();
            work.setId("work_2");
            work.setRetry(10);
            work.setMaxRetries(10);
            work.setScheduledAt(2000);
            work.setIntervalTime(100);
            work.setType("type_1");
            mongoDAO.create(work);
        }
        {
            SchedulableWork work = new SchedulableWork();
            work.setId("work_3");
            work.setRetry(10);
            work.setMaxRetries(10);
            work.setScheduledAt(3000);
            work.setIntervalTime(100);
            work.setType("type_1");
            mongoDAO.create(work);
        }
        {
            SchedulableWork work = new SchedulableWork();
            work.setId("work_4");
            work.setRetry(10);
            work.setMaxRetries(10);
            work.setScheduledAt(4000);
            work.setIntervalTime(100);
            work.setType("type_1");
            mongoDAO.create(work);
        }
        {
            SchedulableWork work = new SchedulableWork();
            work.setId("work_5");
            work.setRetry(10);
            work.setMaxRetries(10);
            work.setScheduledAt(5000);
            work.setIntervalTime(100);
            work.setType("type_1");
            mongoDAO.create(work);
        }
    }
}