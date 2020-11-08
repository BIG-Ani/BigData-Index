package com.neu.info7255.bigdata_proj.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.info7255.bigdata_proj.constant.MessageEnum;
import com.neu.info7255.bigdata_proj.service.AuthorizationService;
import com.neu.info7255.bigdata_proj.service.PlanService;
import com.neu.info7255.bigdata_proj.util.MessageUtil;
import com.neu.info7255.bigdata_proj.validator.SchemaValidator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/demo/v1")
public class PlanController {

    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static SchemaValidator planSchema = new SchemaValidator();

    @Autowired
    private PlanService planService;

    @Autowired
    private AuthorizationService authorizationService;

    @RequestMapping(value = "/{object}", method = RequestMethod.POST)
    public ResponseEntity<String> create(@RequestHeader("Authorization") String idToken,
                                         @PathVariable String object,
                                         @RequestBody String reqJson) {

        logger.info("GOOGLE-ID_TOKEN:" + idToken);

        // authorize
        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");

            String message = MessageUtil.build(MessageEnum.AUTHORIZATION_ERROR);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        // json schema validation
        JSONObject newPlan = new JSONObject(reqJson);
        try {
            logger.info(reqJson);
            planSchema.validateSchema(newPlan);
        } catch (Exception e) {
            logger.info("VALIDATING ERROR: SCHEMA NOT MATCH - " + e.getMessage());

            String message = MessageUtil.build(MessageEnum.VALIDATION_ERROR, e.getMessage());

            return ResponseEntity.badRequest().body(message);
        }

        String internalKey = object + "_" + newPlan.getString("objectId");

        // check exist
        if (planService.hasKey(internalKey)) {

            String message = MessageUtil.build(MessageEnum.CONFLICT_ERROR);

            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }

        logger.info("CREATING NEW DATA: key - " + internalKey + ": " + newPlan.toString());
        planService.savePlan(internalKey, newPlan);

        String message = MessageUtil
                .build(
                        MessageEnum.SAVE_SUCCESS,
                        newPlan.get("objectType") + "_" + newPlan.get("objectId") + " saved");

        return ResponseEntity
                .ok()
                .body(message);
    }

    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<String> patchPlan(@RequestHeader(value = "authorization", required = false) String idToken,
                                             @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                             @PathVariable String object,
                                             @PathVariable String id,
                                             @RequestBody String patchPlan) {

        logger.info("PATCHING PLAN: " + object + ":" + id);

        // check authorization
        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");

            String message = MessageUtil.build(MessageEnum.AUTHORIZATION_ERROR);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        String intervalKey = object + "_" + id;

        // etag check
        String planEtag = planService.getEtag(intervalKey, "eTag");

        if (ifMatch == null) {
            logger.info("HEADER DOES NOT HAVE IF_MATCH");

            String message = MessageUtil.build(MessageEnum.IF_MATCH_MISSING_ERROR);

            return new ResponseEntity<>(message, HttpStatus.PRECONDITION_REQUIRED);
        }

        if (!ifMatch.equals(planEtag)) {
            logger.info("PATCH PLAN CONFLICT");

            String message = MessageUtil.build(MessageEnum.IF_MATCH_ERROR);

            return new ResponseEntity<>(message, HttpStatus.PRECONDITION_FAILED);
        }

        // check plan exist
        if (!planService.hasKey(intervalKey)) {
            logger.info("PATCH PLAN: " + intervalKey + " does not exist");

            String message = MessageUtil.build(MessageEnum.NOT_FOUND_ERROR);

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(message);
        }

        // update plan
        JSONObject patchPlanJson = new JSONObject(patchPlan);

        planService.update(intervalKey, patchPlanJson);
        logger.info("PATCH PLAN : " + intervalKey + " updates successfully");

        String message = MessageUtil.build(MessageEnum.PATCH_SUCCESS);

        return ResponseEntity
                .ok()
                .eTag(planEtag)
                .body(message);
    }

    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.PUT)
    public ResponseEntity<String> updatePlan(@RequestHeader(value = "authorization", required = false) String idToken,
                                             @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                             @PathVariable String object,
                                             @PathVariable String id,
                                             @RequestBody String putPlan) {
        logger.info("PUTTING PLAN");

        // authorization
        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");

            String message = MessageUtil.build(MessageEnum.AUTHORIZATION_ERROR);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        // json schema validation
        JSONObject newPlan = new JSONObject(putPlan);
        try {
            logger.info(putPlan);

            planSchema.validateSchema(newPlan);
        } catch (Exception e) {
            logger.info("VALIDATING ERROR: SCHEMA NOT MATCH - " + e.getMessage());

            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String intervalKey = object + "_" + id;

        // check etag
        String planEtag = planService.getEtag(intervalKey, "eTag");

        if (ifMatch == null) {
            logger.info("HEADER DOES NOT HAVE IF_MATCH");

            String message = MessageUtil.build(MessageEnum.IF_MATCH_MISSING_ERROR);

            return new ResponseEntity<>(message, HttpStatus.PRECONDITION_REQUIRED);
        }

        if (!ifMatch.equals(planEtag)) {
            logger.info("PUT PLAN CONFLICT");

            String message = MessageUtil.build(MessageEnum.IF_MATCH_ERROR);

            return ResponseEntity
                    .status(HttpStatus.PRECONDITION_FAILED)
                    .eTag(planEtag)
                    .body(message);
        }

        // check plan exist
        if (!planService.hasKey(intervalKey)) {
            logger.info("PUT PLAN: " + intervalKey + " does not exist");

            String message = MessageUtil.build(MessageEnum.NOT_FOUND_ERROR);

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(message);
        }

        // delete old plan
        planService.deletePlan(intervalKey);

        // update put plan
        JSONObject putPlanJson = new JSONObject(putPlan);

        planService.savePlan(intervalKey, putPlanJson);
        logger.info("PUT PLAN: " + intervalKey + " updates successfully");

        String message = MessageUtil.build(MessageEnum.PUT_SUCCESS);

        return ResponseEntity
                .ok()
                .eTag(planEtag)
                .body(message);
    }

    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> readByKey(@RequestHeader(value = "Authorization", required = false) String idToken,
                                            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
                                            @PathVariable String object,
                                            @PathVariable String id) {

        logger.info("RETRIEVING REDIS DATA: " + "object - " + object +
                "; id - " + id);

        String internalKey = object + "_" + id;

        // authorize
        logger.info("AUTHORIZATION: GOOGLE_ID_TOKEN: " + idToken);

        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");

            String message = MessageUtil.build(MessageEnum.AUTHORIZATION_ERROR);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        if (!planService.hasKey(internalKey)) {
            logger.info("OBJECT NOT FOUND - " + internalKey);

            String message = MessageUtil.build(MessageEnum.NOT_FOUND_ERROR);

            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }

        Map<String, Object> foundValue;
        foundValue = planService.getPlan(internalKey);

        // e-tag
        String objectEtag = planService.getEtag(internalKey, "eTag");

        if (objectEtag.equals(ifNoneMatch)) {
            logger.info("CACHING AVAILABLE: " + internalKey);

            String message = MessageUtil.build(MessageEnum.IF_MATCH_ERROR);

            return new ResponseEntity<>(message, HttpStatus.NOT_MODIFIED);
        }

        logger.info("OBJECT FOUND - " + internalKey);
        try {

            String value = objectMapper.writeValueAsString(foundValue);

            return ResponseEntity
                    .ok()
                    .eTag(objectEtag)
                    .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                    .body(value);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteByKey(@RequestHeader("authorization") String idToken,
                                              @PathVariable String object,
                                              @PathVariable String id) {

        logger.info("DELETING OBJECT: " + object + ", id - " + id);

        // authorize
        logger.info("AUTHORIZATION: GOOGLE_ID_TOKEN: " + idToken);

        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");

            String message = MessageUtil.build(MessageEnum.AUTHORIZATION_ERROR);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        String intervalKey = object + "_" + id;

        if (!planService.hasKey(intervalKey)) {
            String message = MessageUtil.build(MessageEnum.NOT_FOUND_ERROR);

            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }

        planService.deletePlan(intervalKey);
        logger.info("DELETED SUCCESSFULLY: " + object + "_" + intervalKey);

        String message = MessageUtil.build(MessageEnum.DELETE_SUCCESS);
        return new ResponseEntity<>(message, HttpStatus.OK);

    }
}
