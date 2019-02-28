package org.iblog.enhance.gateway.impl;

import org.iblog.enhance.gateway.core.OpenApi;
import org.iblog.enhance.gateway.db.mongo.OpenApiMongoDAO;
import org.iblog.enhance.gateway.mongo.MongodbDAOTestBase;
import org.iblog.enhance.gateway.service.OpenApiService;
import org.iblog.enhance.gateway.service.impl.OpenApiServiceImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OpenApiServiceImplTest extends MongodbDAOTestBase {
    private OpenApiService openApiService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        OpenApiMongoDAO openApiMongoDAO = new OpenApiMongoDAO(mongoTemplate);
        openApiService = new OpenApiServiceImpl(openApiMongoDAO);
    }

    @Test
    public void testUriPatternAndMethod() {
        {
            OpenApi api = new OpenApi();
            api.setUriPattern("/evo-interface/_internal/logging-record");
            api.setMethod("POST");
            api.setFrom("external");
            api.setTo("interface");
            api.setRegex(false);
            openApiService.create(api);
        }

        assertTrue(openApiService.exist(
                "/evo-interface/_internal/logging-record", "POST"));
        OpenApi find = openApiService.find(
                "/evo-interface/_internal/logging-record", "POST").orNull();
        assertNotNull(find);
        assertEquals("/evo-interface/_internal/logging-record", find.getUriPattern());
        assertEquals("POST", find.getMethod());

        {
            OpenApi api = new OpenApi();
            api.setUriPattern("/evo-interface/_internal/logging-record/*");
            api.setMethod("GET");
            api.setFrom("external");
            api.setTo("interface");
            api.setRegex(true);
            openApiService.create(api);
        }
        {
            OpenApi api = new OpenApi();
            api.setUriPattern("/evo-interface/_internal/logging-record/*");
            api.setMethod("PATCH");
            api.setFrom("external");
            api.setTo("interface");
            api.setRegex(true);
            openApiService.create(api);
        }

        assertFalse(openApiService.exist(
                "/evo-interface/_internal/logging-record/123456", "POST"));
        assertTrue(openApiService.exist(
                "/evo-interface/_internal/logging-record/123456", "GET"));
        assertTrue(openApiService.exist(
                "/evo-interface/_internal/logging-record/123456", "PATCH"));

        find = openApiService.find(
                "/evo-interface/_internal/logging-record/123456", "POST").orNull();
        assertNull(find);
        find = openApiService.find(
                "/evo-interface/_internal/logging-record/123456", "GET").orNull();
        assertNotNull(find);
        find = openApiService.find(
                "/evo-interface/_internal/logging-record/123456", "PATCH").orNull();
        assertNotNull(find);
    }

    @Test
    public void testCreate() {
        OpenApi api = new OpenApi();
        api.setUriPattern("/evo-interface/_internal/logging-record/*");
        api.setMethod("GET");
        api.setFrom("external");
        api.setTo("interface");
        api.setRegex(true);
        openApiService.create(api);
        OpenApi exist = openApiService.find(
                "/evo-interface/_internal/logging-record/*", "GET").orNull();
        assertNotNull(exist);
        assertTrue(exist.getCreatedAt() > 0);
        assertEquals(exist.getCreatedAt(), exist.getLastUpdatedAt());
    }

    @Test
    public void testExist() {
        OpenApi api = new OpenApi();
        api.setUriPattern("/evo-interface/_internal/logging-record/*");
        api.setMethod("GET");
        api.setFrom("external");
        api.setTo("interface");
        api.setRegex(true);
        openApiService.create(api);
        OpenApi exist = openApiService.find(
                "/evo-interface/_internal/logging-record/*", "GET").orNull();
        assertNotNull(exist);
        assertTrue(openApiService.exist(exist.getId()));
    }

    @Test
    public void testUpdate() {
        OpenApi api = new OpenApi();
        api.setUriPattern("/evo-interface/_internal/logging-record/*");
        api.setMethod("GET");
        api.setFrom("external");
        api.setTo("interface");
        api.setRegex(true);
        openApiService.create(api);
        OpenApi exist = openApiService.find(
                "/evo-interface/_internal/logging-record/*", "GET").orNull();
        assertNotNull(exist);
        long updatedAt = exist.getLastUpdatedAt();
        assertTrue(updatedAt > 0);
        exist.setMethod("POST");
        openApiService.update(exist);
        OpenApi updated = openApiService.find(exist.getId()).orNull();
        assertNotNull(updated);
        assertTrue(updated.getLastUpdatedAt() > updatedAt);
    }

    @Test
    public void testDelete() {
        OpenApi api = new OpenApi();
        api.setUriPattern("/evo-interface/_internal/logging-record/*");
        api.setMethod("GET");
        api.setFrom("external");
        api.setTo("interface");
        api.setRegex(true);
        openApiService.create(api);
        OpenApi exist = openApiService.find(
                "/evo-interface/_internal/logging-record/*", "GET").orNull();
        assertTrue(openApiService.exist(exist.getId()));
        openApiService.delete(exist.getId());
        assertFalse(openApiService.exist(exist.getId()));
    }

    @Test
    public void testList() {
        // TODO skip
    }
}