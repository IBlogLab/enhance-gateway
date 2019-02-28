package org.iblog.enhance.gateway.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.iblog.enhance.gateway.lifecycle.sched.NamedThreadFactory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExecutorUtilTest {

    @Test
    public void testCatchError() throws Exception {
        AtomicInteger reached = new AtomicInteger();
        AtomicReference<Object> caught = new AtomicReference<>();
        ExecutorService executor = new ThreadPoolExecutor(
                1, 1, 0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new NamedThreadFactory("ExecutorUtilTest")) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                ExecutorUtil.afterExecute2(r, t, (r2, t2) -> {
                    reached.incrementAndGet();
                    caught.set(t2);
                });
            }
        };

        CountDownLatch done = new CountDownLatch(1);
        executor.submit(() -> done.countDown());
        done.await();
        assertFalse(ExecutorUtil.waitUntilTrue(() -> reached.get() > 0, 1000));
        assertNull(caught.get());

        executor.submit(() -> {
            throw new OutOfMemoryError("fake OOM");
        });
        assertTrue(ExecutorUtil.waitUntilTrue(() -> (caught.get() instanceof  OutOfMemoryError), 1000));
    }
}