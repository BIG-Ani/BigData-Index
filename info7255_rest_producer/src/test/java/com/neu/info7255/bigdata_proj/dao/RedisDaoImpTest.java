package com.neu.info7255.bigdata_proj.dao;

import com.neu.info7255.bigdata_proj.BigdataProjApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class RedisDaoImpTest {

    @Autowired
    RedisDaoImp redisDaoImp;

    private static String key = "user";
    private static String value = "lei";

    @Test
    void putValue() {
        redisDaoImp.putValue(key, value);
    }
}