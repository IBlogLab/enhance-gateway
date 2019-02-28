package org.iblog.enhance.gateway.lifecycle.sched;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.iblog.enhance.gateway.util.ExecutorUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GlobalExecutorTest {
    GlobalExecutor globalExecutor;

    @Before
    public void setUp() throws Exception {
        globalExecutor = new GlobalExecutor();
    }

    @After
    public void tearDown() throws Exception {
        globalExecutor.stop();
    }

    @Test
    public void testHandleUnexpectedError() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        globalExecutor.schedule(() -> {
            if (counter.getAndIncrement() % 2 == 0) {
                throw new RuntimeException("Injected " + counter.get());
            }
        }, 0, 100);

        assertTrue(ExecutorUtil.waitUntilTrue(() -> counter.get() == 4, 2000));
    }

    @Test
    public void testHandleUnexpectedErrorInSubmission() throws Exception {
        Future<?> f0  = globalExecutor.submit(() -> {
            throw new RuntimeException("crap");
        });

        Future<Integer> f1  = globalExecutor.submit(() -> 10);

        boolean caught = false;
        try {
            f0.get();
        } catch (ExecutionException e) {
            caught = true;
        }
        assertTrue(caught);
        assertEquals(10, f1.get().intValue());
    }
}