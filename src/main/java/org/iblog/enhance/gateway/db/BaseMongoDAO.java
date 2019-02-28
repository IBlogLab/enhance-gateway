package org.iblog.enhance.gateway.db;

import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author lance
 */
public class BaseMongoDAO {
    protected final MongoTemplate mongoTemplate;

    public BaseMongoDAO(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
