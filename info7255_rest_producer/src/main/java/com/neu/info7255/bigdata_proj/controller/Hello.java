package com.neu.info7255.bigdata_proj.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {

    private static final Logger logger = LoggerFactory.getLogger(Hello.class);

    @GetMapping(value = "/")
    public String hello() {
        logger.info("Hello Index Page");
        return "hello, world!";
    }

}
