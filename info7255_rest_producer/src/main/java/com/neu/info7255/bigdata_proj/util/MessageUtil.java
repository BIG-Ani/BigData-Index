package com.neu.info7255.bigdata_proj.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.info7255.bigdata_proj.constant.MessageEnum;

import java.util.HashMap;
import java.util.Map;

public class MessageUtil extends Message{

    private static ObjectMapper objectMapper = new ObjectMapper();

    public MessageUtil(String msgHeader, String msgBody) {
        super(msgHeader, msgBody);
    }

    public static String build(MessageEnum messageEnum) {
        Map<String, String> msgMap = new HashMap<>();

        msgMap.put(messageEnum.getHeader(), messageEnum.getBody());

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(msgMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return msg;
    }

    public static String build(MessageEnum messageEnum, String body) {
        Map<String, String> msgMap = new HashMap<>();

        msgMap.put(messageEnum.getHeader(), body);

        String msg = "";
        try {
            msg = objectMapper.writeValueAsString(msgMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return msg;
    }

    public static String build(String header, String body) {
        Map<String, String> msg = new HashMap<>();
        msg.put(header, body);

        String msgJson = "";
        try {
            msgJson = objectMapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return msgJson;
    }

}
