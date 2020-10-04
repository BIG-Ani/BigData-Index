package com.neu.info7255.bigdata_proj.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
public class RedisDaoImp implements RedisDao {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void putValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public boolean deleteValue(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
