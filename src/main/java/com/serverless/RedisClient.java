package com.serverless;

import redis.clients.jedis.Jedis;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

class RedisClient {
    private static final String REDIS_HOST = System.getenv("REDIS_HOST");
    private static final Integer REDIS_PORT = new Integer(defaultIfBlank(System.getenv("REDIS_PORT"), "6379"));
    private Jedis redis;

    RedisClient() {
        this.redis = new Jedis(REDIS_HOST, REDIS_PORT);
    }

    void set(String key, String value) {
        this.redis.set(key, value);
    }

    String get(String key) {
        return this.redis.get(key);
    }
}
