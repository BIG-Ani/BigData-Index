package com.neu.info7255.bigdata_proj.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.info7255.bigdata_proj.service.RedisServiceImp;
import com.neu.info7255.bigdata_proj.util.MessageDigestGenerator;
import com.neu.info7255.bigdata_proj.util.MessageUtil;
import com.neu.info7255.bigdata_proj.validator.SchemaValidator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.tags.form.SelectTag;

import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/plan/v1")
public class PlanController {

    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RedisServiceImp redisService;

    @RequestMapping(value = "/{object}", method = RequestMethod.POST)
    public ResponseEntity<String> create(@PathVariable String object,
                                         @RequestBody String reqJson,
                                         HttpEntity<String> req) {

        SchemaValidator planSchema = new SchemaValidator();

        logger.info("REQUEST BODY:" + req.getBody());

        try {
            logger.info(reqJson);
            planSchema.validateSchema(new JSONObject(reqJson));
        } catch (Exception e) {
            logger.info("VALIDATING ERROR: SCHEMA NOT MATCH - " + e.getMessage());

            return ResponseEntity.badRequest().body(e.getMessage());
        }

        JSONObject jsonObject = new JSONObject(reqJson);

        String internalKey = object + "_objectId_" + jsonObject.getString("objectId");
        redisService.create(internalKey, jsonObject.toString());

        logger.info("CREATING NEW DATA: key - " + internalKey + ": json - " + jsonObject.toString());
        return ResponseEntity.ok().body(" {\"message\": \"Created data with key: " + internalKey + "\" }");
    }

    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> readByKey(@PathVariable String object,
                                            @PathVariable String id,
                                            WebRequest webRequest) {
        logger.info("RETRIEVING REDIS DATA: " + "object - " + object +
                "; id - " + id);

        String intervalKey = object + "_objectId_" + id;

        String foundValue;
        try {
            foundValue = redisService.read(intervalKey);
        } catch (Exception e) {
            logger.info("OBJECT NOT FOUND - " + intervalKey);
            return ResponseEntity.badRequest().body("Object not found");
        }

        if (webRequest.checkNotModified(webRequest.getHeader("If-None-Match"))) {
            logger.info("CACHING AVAILABLE: " + intervalKey);
            return new ResponseEntity<>("{}", HttpStatus.NOT_MODIFIED);
        }

        logger.info("OBJECT FOUND - " + intervalKey);
        try {
            JsonNode jsonNode = objectMapper.readTree(foundValue);

            foundValue = jsonNode.toString();
            return ResponseEntity
                    .ok()
                    .eTag(MessageDigestGenerator.getSequence("MD5", intervalKey))
                    .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                    .body(foundValue);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteByKey(@PathVariable String object,
                                              @PathVariable String id) {
        logger.info("DELETING OBJECT: " + object + ", id - " + id);

        String intervalKey = object + "_objectId_" + id;

        boolean deleteRes = redisService.delete(intervalKey);
        if (deleteRes) {
            return new ResponseEntity<>("{\"message\":" + "\"" + intervalKey + " Deleted\"}", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(" {\"message\": \"item not found\" }", HttpStatus.NOT_FOUND);
        }
    }
}
