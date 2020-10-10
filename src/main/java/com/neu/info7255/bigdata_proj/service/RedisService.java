package com.neu.info7255.bigdata_proj.service;

public interface RedisService {

    void create(String key, String value);

    boolean delete(String key);

    String read(String key);

}
