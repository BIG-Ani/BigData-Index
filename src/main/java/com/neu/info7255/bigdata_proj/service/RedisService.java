package com.neu.info7255.bigdata_proj.service;

public interface RedisService {

    public void create(String key, String value);

    public boolean delete(String key);

    public String read(String key);

}
