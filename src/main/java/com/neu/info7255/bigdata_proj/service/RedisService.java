package com.neu.info7255.bigdata_proj.service;

import org.json.JSONObject;

public interface RedisService {

    void create(String key, String value);

    boolean delete(String key);

    String read(String key);

    boolean hasKey(String key);

    String savePlan(String key, JSONObject object);

    void deletePlan(String key);

    void update(String key, JSONObject object);

    void update(JSONObject object);

    String getEtag(String key, String etag);

}
