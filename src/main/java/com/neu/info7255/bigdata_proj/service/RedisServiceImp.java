package com.neu.info7255.bigdata_proj.service;

import com.neu.info7255.bigdata_proj.dao.RedisDaoImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImp implements RedisService {

    private static Logger logger = LoggerFactory.getLogger(RedisServiceImp.class);

    @Autowired
    private RedisDaoImp redisDao;

    @Override
    public void create(String key, String value) {
        logger.info("CREATING NEW DATA: [" + key + " - " + value + "]");
        redisDao.putValue(key, value);
    }

    @Override
    public boolean delete(String key) {
        logger.info("DELETING DATA - KEY: " + key);
        return redisDao.deleteValue(key);
    }

    @Override
    public String read(String key) {
        logger.info("READING DATA - KEY: " + key);
        return redisDao.getValue(key).toString();
    }
}