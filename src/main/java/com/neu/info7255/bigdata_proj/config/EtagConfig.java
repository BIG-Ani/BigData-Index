package com.neu.info7255.bigdata_proj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.Filter;

//@Configuration
public class EtagConfig {

//    @Bean
    public Filter getEtagFilter() {
        ShallowEtagHeaderFilter shallowEtagHeaderFilter = new ShallowEtagHeaderFilter();

        return shallowEtagHeaderFilter;
    }

}
