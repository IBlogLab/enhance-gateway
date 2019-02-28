package org.iblog.enhance.gateway.mongo;

import java.util.List;
import java.util.stream.Collectors;
import org.iblog.enhance.gateway.core.OpenApi;
import org.iblog.enhance.gateway.db.filter.OpenApiFilter;
import org.iblog.enhance.gateway.db.mongo.OpenApiMongoDAO;
import org.junit.Test;
import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OpenApiMongoDAOTest extends MongodbDAOTestBase {
    private OpenApiMongoDAO mongoDAO;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mongoDAO = new OpenApiMongoDAO(mongoTemplate);
    }

    @Test
    public void testCreate() {
        OpenApi api = new OpenApi();
        api.setId("0002");
        api.setUriPattern("/evo-interface/open-api");
        api.setMethod("POST");
        api.setApiCode("test_api");
        api.setBusinessType("create");
        mongoDAO.create(api);
        OpenApi insert = mongoDAO.find("0002");
        assertNotNull(insert);
        assertEquals("0002", insert.getId());
    }

    @Test
    public void testExist() {
        OpenApi api = new OpenApi();
        api.setId("0002");
        api.setUriPattern("/evo-interface/open-api");
        api.setMethod("POST");
        api.setApiCode("test_api");
        api.setBusinessType("create");
        mongoDAO.create(api);
        assertTrue(mongoDAO.exist("0002"));
        assertFalse(mongoDAO.exist("0001"));
    }

    @Test
    public void testUpdate() {
        OpenApi api = new OpenApi();
        api.setId("0002");
        api.setUriPattern("/evo-interface/open-api");
        api.setMethod("POST");
        api.setApiCode("test_api");
        api.setBusinessType("create");
        mongoDAO.create(api);
        OpenApi exist = mongoDAO.find("0002");
        assertNotNull(exist);

        exist.setLastUpdatedAt(1000);
        exist.setLastUpdatedBy("liming");
        exist.setMethod("PATCH");
        assertEquals(1, mongoDAO.update(exist));
    }

    @Test
    public void testDelete() {
        OpenApi api = new OpenApi();
        api.setId("0002");
        api.setUriPattern("/evo-interface/open-api");
        api.setMethod("POST");
        api.setApiCode("test_api");
        api.setBusinessType("create");
        mongoDAO.create(api);
        OpenApi exist = mongoDAO.find("0002");
        assertNotNull(exist);

        mongoDAO.delete("0002");
        exist = mongoDAO.find("0002");
        assertNull(exist);
    }

    @Test
    public void testExistFilter() {
        initData();

        OpenApiFilter.Builder builder = new OpenApiFilter.Builder();
        builder.setStart(-1000).setEnd(0);
        assertFalse(mongoDAO.exist(builder.build()));

        builder.setStart(0).setEnd(2000);
        assertTrue(mongoDAO.exist(builder.build()));
        builder.setUris(Lists.newArrayList("test_null"));
        assertFalse(mongoDAO.exist(builder.build()));

        builder.setStart(0).setEnd(5000)
                .setUris(null)
                .setMethods(Lists.newArrayList("GET", "POST"));
        assertTrue(mongoDAO.exist(builder.build()));

        builder.setStart(0).setEnd(5000)
                .setUris(null)
                .setMethods(null)
                .setCodes(Lists.newArrayList("code_2"));
        assertTrue(mongoDAO.exist(builder.build()));
    }

    @Test
    public void testList() {
        initData();

        OpenApiFilter.Builder builder = new OpenApiFilter.Builder();
        List<OpenApi> list = mongoDAO.list(builder.build());
        assertNotNull(list);
        assertEquals(20, list.size());

        builder.setStart(1000).setEnd(3000);
        list = mongoDAO.list(builder.build());
        assertNotNull(list);
        assertEquals(14, list.size());

        builder.setStart(0).setEnd(5000)
                .setUris(Lists.newArrayList("/evo-interface/test/1", "/evo-interface/test/4", "/evo-interface/test/19"));
        list = mongoDAO.list(builder.build());
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(Lists.newArrayList("/evo-interface/test/1", "/evo-interface/test/4", "/evo-interface/test/19"),
                list.stream().map(OpenApi::getUriPattern).collect(Collectors.toList()));

        builder.setUris(null)
                .setMethods(Lists.newArrayList("PATCH"));
        list = mongoDAO.list(builder.build());
        assertNotNull(list);
        assertEquals(6, list.size());

        builder.setCodes(Lists.newArrayList("code_0"));
        list = mongoDAO.list(builder.build());
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    public void testCount() {
        initData();

        OpenApiFilter.Builder builder = new OpenApiFilter.Builder();
        builder.setStart(1000).setEnd(3000);
        assertEquals(14, mongoDAO.count(builder.build()));
    }

    @Test
    public void testPageable() {
        initData();
        OpenApiFilter.Builder builder = new OpenApiFilter.Builder();
        builder.setFrom(0);
        builder.setTo(10);
        List<OpenApi> list = mongoDAO.list(builder.build());
        assertNotNull(list);
        assertEquals(10, list.size());
    }

    private void initData() {
        for (int i = 0; i < 20; i++) {
            OpenApi api = new OpenApi();
            api.setId(String.valueOf(i));
            api.setUriPattern("/evo-interface/test/" + i);
            if (i % 3 == 0) {
                api.setMethod("GET");
                api.setApiCode("code_" + 0);
                api.setBusinessType("type_" + 0);
                api.setCreatedAt(1000);
            } else if (i % 3 == 1) {
                api.setMethod("POST");
                api.setApiCode("code_" + 1);
                api.setBusinessType("type_" + 1);
                api.setCreatedAt(2000);
            } else if (i % 3 == 2) {
                api.setMethod("PATCH");
                api.setApiCode("code_" + 2);
                api.setBusinessType("type_" + 2);
                api.setCreatedAt(3000);
            }
            mongoDAO.create(api);
        }
    }

}