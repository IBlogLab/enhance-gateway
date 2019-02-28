package org.iblog.enhance.gateway.mongo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import com.mongodb.MongoClient;

import static org.junit.Assume.assumeTrue;

/**
 * @author lance
 */
public class MongodbDAOTestBase {

    private static MongoClient mongoClient;
    private static SimpleMongoDbFactory dbFactory;
    protected static MongoTemplate mongoTemplate;

    @BeforeClass
    public static void setUpTestCase() throws Exception {
        if (!useMongodb()) {
            mongoTemplate = null;
            return;
        }

        mongoClient = new MongoClient("localhost");
        dbFactory = new SimpleMongoDbFactory(mongoClient, "unittest");
        mongoTemplate = new MongoTemplate(dbFactory);
    }

    @AfterClass
    public static void tearDownTestCase() throws Exception {
        mongoClient = null;
        dbFactory = null;
        mongoTemplate = null;
    }

    @Before
    public void setUp() throws Exception {
        assumeTrue(useMongodb());
    }

    @After
    public void tearDown() throws Exception {
        if (dbFactory != null) {
            dbFactory.getDb("unittest").drop();
            dbFactory.destroy();
        }
    }

    private static boolean useMongodb() {
        boolean db = Boolean.parseBoolean(
                System.getProperty("test.db.mongo", "false"));
        if (!db) {
            return false;
        }
        return true;
    }

}
