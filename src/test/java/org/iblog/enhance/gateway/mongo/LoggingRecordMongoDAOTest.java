package org.iblog.enhance.gateway.mongo;

import java.util.ArrayList;
import java.util.List;
import org.iblog.enhance.gateway.core.BusinessType;
import org.iblog.enhance.gateway.core.KeyWord;
import org.iblog.enhance.gateway.core.LoggingRecord;
import org.iblog.enhance.gateway.db.filter.LoggingRecordFilter;
import org.iblog.enhance.gateway.db.mongo.LoggingRecordMongoDAO;
import org.iblog.enhance.gateway.core.Error;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LoggingRecordMongoDAOTest extends MongodbDAOTestBase {
    private LoggingRecordMongoDAO mongoDAO;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mongoDAO = new LoggingRecordMongoDAO(mongoTemplate);
    }

    @Test
    public void find() {
        LoggingRecord record = new LoggingRecord();
        record.setId("1");
        record.setUri("/evo-interface/logging-record");
        record.setApiCode("logging-record-post");
        record.setMethod(HttpMethod.POST);
        record.setCreatedAt(1000);
        record.setTags(Lists.newArrayList(KeyWord.build("orderId", "123456")));
        mongoDAO.create(record);

        LoggingRecord insert = mongoDAO.find("1");
        assertNotNull(insert);
        assertEquals("/evo-interface/logging-record", insert.getUri());
        assertEquals(1000, insert.getCreatedAt());
    }

    @Test
    public void exist() {
        LoggingRecord record = new LoggingRecord();
        record.setId("1");
        record.setUri("/evo-interface/logging-record");
        record.setApiCode("logging-record-post");
        record.setMethod(HttpMethod.POST);
        record.setCreatedAt(1000);
        record.setTags(Lists.newArrayList(KeyWord.build("orderId", "123456")));
        mongoDAO.create(record);
        assertTrue(mongoDAO.exist("1"));
    }

    @Test
    public void delete() {
        LoggingRecord record = new LoggingRecord();
        record.setId("1");
        record.setUri("/evo-interface/logging-record");
        record.setApiCode("logging-record-post");
        record.setMethod(HttpMethod.POST);
        record.setCreatedAt(1000);
        record.setTags(Lists.newArrayList(KeyWord.build("orderId", "123456")));
        mongoDAO.create(record);
        LoggingRecord insert = mongoDAO.find("1");
        assertNotNull(insert);

        mongoDAO.delete("1");
        insert = mongoDAO.find("1");
        assertNull(insert);
    }

    @Test
    public void update() {
        LoggingRecord record = new LoggingRecord();
        record.setId("1");
        record.setUri("/evo-interface/logging-record");
        record.setApiCode("logging-record-post");
        record.setMethod(HttpMethod.POST);
        record.setCreatedAt(1000);
        record.setTags(Lists.newArrayList(KeyWord.build("orderId", "123456")));
        mongoDAO.create(record);

        LoggingRecord insert = mongoDAO.find("1");
        assertNotNull(insert);
        assertEquals("/evo-interface/logging-record", insert.getUri());
        assertEquals(1000, insert.getCreatedAt());
        assertEquals(Lists.newArrayList("123456"), insert.getTags().get(0).getValues());

        insert.setLastUpdatedAt(2000);
        insert.setAsync(true);
        insert.setBusinessType(BusinessType.UNKNOWN.toString());
        insert.setError(Error.OK);
        insert.setFrom("DHL");
        insert.setTo("WES");
        insert.setTags(Lists.newArrayList(
                KeyWord.build("orderId", "567890"),
                KeyWord.build("sku", "iphone8", "MacBook pro 2018")));
        mongoDAO.update(insert);

        LoggingRecord update = mongoDAO.find("1");
        assertNotNull(update);
        assertEquals(2000, update.getLastUpdatedAt());
        assertTrue(update.isAsync());
        assertEquals(BusinessType.UNKNOWN.name(), update.getBusinessType());
        assertEquals(Error.OK, update.getError());
        assertEquals("DHL", update.getFrom());
        assertEquals("WES", update.getTo());
        assertEquals(Lists.newArrayList("567890"), update.getTags().get(0).getValues());
        assertEquals(Lists.newArrayList("iphone8", "MacBook pro 2018"), update.getTags().get(1).getValues());
    }

    @Test
    public void existFilter() {
        initData();
        LoggingRecordFilter.Builder builder = new LoggingRecordFilter.Builder();
        boolean exist = mongoDAO.exist(builder.build());
        assertTrue(exist);

        builder.setFroms(Lists.newArrayList("from1"));
        assertTrue(mongoDAO.exist(builder.build()));
        builder.setFroms(Lists.newArrayList("from21"));
        assertFalse(mongoDAO.exist(builder.build()));
        builder.setFroms(Lists.newArrayList("from1", "from21"));
        assertTrue(mongoDAO.exist(builder.build()));
    }

    @Test
    public void listTags() {
        {
            LoggingRecord record = new LoggingRecord();
            record.setId("1");
            record.setMethod(HttpMethod.GET);
            record.setTags(Lists.newArrayList(
                    KeyWord.build("sku", "mac pro", "iphone8", "iphone xr", "m3"),
                    KeyWord.build("order", "12", "34"),
                    KeyWord.build("operator", "lance", "tom", "tony")));
            mongoDAO.create(record);
        }
        {
            LoggingRecord record = new LoggingRecord();
            record.setId("2");
            record.setMethod(HttpMethod.GET);
            record.setTags(Lists.newArrayList(
                    KeyWord.build("order", "67"),
                    KeyWord.build("sku", "A4 paper", "mark pencil", "iphone xr"),
                    KeyWord.build("producing area", "Zhejiang", "Shenzhen")));
            mongoDAO.create(record);
        }
        {
            LoggingRecord record = new LoggingRecord();
            record.setId("3");
            record.setMethod(HttpMethod.POST);
            record.setTags(Lists.newArrayList(
                    KeyWord.build("address", "Hubei Wuhan", "Zhejiang ningbo")));
            mongoDAO.create(record);
        }
        {
            LoggingRecord record = new LoggingRecord();
            record.setId("4");
            record.setMethod(HttpMethod.POST);
            record.setTags(Lists.newArrayList(
                    KeyWord.build("address", "Hubei Xianning", "Shanghai Xuhui"),
                    KeyWord.build("category", "10001", "20001", "30001")));
            mongoDAO.create(record);
        }
        {
            LoggingRecord record = new LoggingRecord();
            record.setId("5");
            record.setMethod(HttpMethod.POST);
            record.setTags(Lists.newArrayList(
                    KeyWord.build("category", "20001", "40001"),
                    KeyWord.build("producing area", "Zhejiang", "Anhui")));
            mongoDAO.create(record);
        }

        LoggingRecordFilter.Builder builder = new LoggingRecordFilter.Builder();
        List<LoggingRecord> list = mongoDAO.list(builder.build());
        assertNotNull(list);
        assertEquals(5, list.size());

        builder.setTags(Lists.newArrayList(
                KeyWord.build("sku", "hone"),
                KeyWord.build("category", "40001")));
        builder.setMethods(Lists.newArrayList(HttpMethod.POST, HttpMethod.GET));
        list = mongoDAO.list(builder.build());
        assertNotNull(list);
        assertEquals(3, list.size());
    }

    @Test
    public void count() {
        initData();
        LoggingRecordFilter.Builder builder = new LoggingRecordFilter.Builder();
        builder.setUris(Lists.newArrayList("/evo-interface/logging-record/*"));
        long count = mongoDAO.count(builder.build());
        assertEquals(10, count);

        builder.setUris(Lists.newArrayList("/evo-interface/logging-record/*", "/evo-interface/open-api/*"));
        count = mongoDAO.count(builder.build());
        assertEquals(20, count);
    }

    @Test
    public void testDelete() {
        initData();
        long count = mongoDAO.count(new LoggingRecordFilter.Builder().build());
        assertEquals(20, count);

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ids.add(String.valueOf(i));
        }
        long delete = mongoDAO.delete(ids);
        assertEquals(20, delete);
        count = mongoDAO.count(new LoggingRecordFilter.Builder().build());
        assertEquals(0, count);
    }

    private void initData() {
        for (int i = 0; i < 20; i++) {
            LoggingRecord record = new LoggingRecord();
            record.setId(String.valueOf(i));
            record.setBusinessType(BusinessType.UNKNOWN.toString());
            if (i % 2 == 0) {
                record.setUri("/evo-interface/logging-record/" + i);
                record.setAsync(true);
            } else {
                record.setUri("/evo-interface/open-api/" + i);
                record.setAsync(false);
            }
            record.setError(Error.OK);
            if (i % 4 == 0) {
                record.setMethod(HttpMethod.GET);
            } else if (i % 4 == 1) {
                record.setMethod(HttpMethod.POST);
            } else if (i % 4 == 2) {
                record.setMethod(HttpMethod.PATCH);
            } else {
                record.setMethod(HttpMethod.DELETE);
            }
            if (i % 7 == 0) {
                record.setStatusCode(400);
            } else {
                record.setStatusCode(200);
            }
            record.setFrom("from" + i % 10);
            record.setTo("to" + i % 8);
            record.setStartTime(1000 * (i + 1));
            mongoDAO.create(record);
        }
    }
}