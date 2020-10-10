package com.neu.info7255.bigdata_proj.dao;

public interface RedisDao {

    void putValue(String key, String value);

    boolean deleteValue(String key);

    Object getValue(String key);
}
