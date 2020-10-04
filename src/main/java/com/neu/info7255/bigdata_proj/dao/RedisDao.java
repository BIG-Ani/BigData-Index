package com.neu.info7255.bigdata_proj.dao;

public interface RedisDao {

    public void putValue(String key, String value);

    public boolean deleteValue(String key);

    public Object getValue(String key);
}
