package com.neu.info7255.bigdata_proj.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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

    @Override
    public void hSet(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    @Override
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void deleteKeys(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    @Override
    public Set<String> keys(String keyPattern) {
        return redisTemplate.keys(keyPattern);
    }

    @Override
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void hDelete(String key, String field) {
        redisTemplate.opsForHash().delete(key, field);
    }

    @Override
    public void setAdd(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public void hDelete(String key) {
        redisTemplate.opsForHash().delete(key);
    }

    @Override
    public Set<String> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public String hGet(String key, String field) {
        return (String) redisTemplate.opsForHash().get(key, field);
    }

}
