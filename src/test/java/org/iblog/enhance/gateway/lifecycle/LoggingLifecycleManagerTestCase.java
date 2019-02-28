package org.iblog.enhance.gateway.lifecycle;

import org.junit.After;
import org.junit.Before;

public class LoggingLifecycleManagerTestCase {
    private LoggingLifecycleManager lifecycleManager;

    @Before
    public void setUp() throws Exception {
        lifecycleManager = new LoggingLifecycleManager();
        lifecycleManager.start();
    }

    @After
    public void tearDown() throws Exception {
        lifecycleManager.destroy();
    }

    // TODO add unittests for others
}