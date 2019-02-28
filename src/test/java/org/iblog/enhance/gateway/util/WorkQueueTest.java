package org.iblog.enhance.gateway.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.iblog.enhance.gateway.core.PageResult;
import org.iblog.enhance.gateway.core.SchedulableWork;
import org.iblog.enhance.gateway.db.filter.SchedulableWorkFilter;
import org.iblog.enhance.gateway.db.mongo.SchedulableWorkMongoDAO;
import org.iblog.enhance.gateway.mongo.MongodbDAOTestBase;
import org.iblog.enhance.gateway.service.SchedulableWorkService;
import org.iblog.enhance.gateway.service.impl.SchedulableWorkServiceImpl;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import com.google.common.util.concurrent.Uninterruptibles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WorkQueueTest extends MongodbDAOTestBase {

    private SchedulableWorkService schedulableWorkService;
    private WorkQueue workQueue;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        SchedulableWorkMongoDAO mongoDAO = new SchedulableWorkMongoDAO(mongoTemplate);
        schedulableWorkService = new SchedulableWorkServiceImpl(mongoDAO);
        workQueue = new WorkQueue("test_work_queue", schedulableWorkService);
    }

    @Test
    public void testScheduleAtIfAbsent() {
        DateTimeUtils.setCurrentMillisFixed(9000000000000L);

        for (int i = 0; i < 20; i++) {
            SchedulableWork work = new SchedulableWork();
            work.setId(i + "");
            work.setType("test");
            work.setScheduledAt(DateTimeUtils.currentTimeMillis() + 100 * (i + 1));
            workQueue.scheduleAtIfAbsent(work);
        }

        PageResult result = schedulableWorkService.list(new SchedulableWorkFilter.Builder().build()).build();
        assertNotNull(result);
        assertEquals(20, result.getTotalCount());
    }

    @Test
    public void testConcurrencyPollIf() {
        final Clock clock = Clock.defaultClock();
        SchedulableWork work = new SchedulableWork();
        work.setId(1 + "");
        work.setType("test");
        work.setMaxRetries(10);
        work.setScheduledAt(clock.getTime() + 100);
        workQueue.scheduleAtIfAbsent(work);

        AtomicInteger integer = new AtomicInteger();
        for (int i = 0; i < 20; i++) {
            String possessor = "possessor" + i;
            new Thread(() -> {
                SchedulableWork exist = workQueue.pollIf(possessor);
                if (exist != null) {
                    integer.incrementAndGet();
                }
            }).start();
        }

        Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);

        assertEquals(1, integer.get());
    }
}