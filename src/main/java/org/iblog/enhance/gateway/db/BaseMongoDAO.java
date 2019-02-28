package org.iblog.enhance.gateway.db;

import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author shaoxiao.xu
 * @date 2018/12/28 19:07
 */
public class BaseMongoDAO {
    protected final MongoTemplate mongoTemplate;

    public BaseMongoDAO(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
