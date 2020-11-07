package com.neu.info7255.bigdata_proj.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.info7255.bigdata_proj.service.AuthorizationService;
import com.neu.info7255.bigdata_proj.service.PlanService;
import com.neu.info7255.bigdata_proj.util.MessageDigestGenerator;
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

import java.nio.file.LinkOption;
import java.util.HashMap;
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
                                         @RequestBody String reqJson,
                                         HttpEntity<String> req) {

        logger.info("GOOGLE-ID_TOKEN:" + idToken);

        // authorize
        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");

            return new ResponseEntity<>(" {\"message\": \"invalid token\" }", HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        // json schema validation
        JSONObject newPlan = new JSONObject(reqJson);
        try {
            logger.info(reqJson);
            planSchema.validateSchema(newPlan);
        } catch (Exception e) {
            logger.info("VALIDATING ERROR: SCHEMA NOT MATCH - " + e.getMessage());

            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String internalKey = object + "_" + newPlan.getString("objectId");

        // check exist
        if (planService.hasKey(internalKey)) {
            return new ResponseEntity<>(new JSONObject().put("message", "item exist").toString(), HttpStatus.CONFLICT);
        }

        logger.info("CREATING NEW DATA: key - " + internalKey + ": " + newPlan.toString());
        planService.savePlan(internalKey, newPlan);

        String res = "{ObjectId: " + newPlan.get("objectId") + ", ObjectType: " + newPlan.get("objectType") + "}";
        return ResponseEntity.ok().body(new JSONObject(res).toString());
    }

    // PATCH PLAN
    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<String> patchPlan(@RequestHeader("authorization") String idToken,
                                             @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
                                             @PathVariable String object,
                                             @PathVariable String id,
                                             @RequestBody String patchPlan) {

        logger.info("PATCHING PLAN: " + object + ":" + id);

        // check authorization
        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");
            return new ResponseEntity<>(" {\"message\": \"invalid token\" }", HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        // check plan exist
        String intervalKey = object + "_" + id;

        if (!planService.hasKey(intervalKey)) {
            logger.info("PATCH PLAN: " + intervalKey + " does not exist");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject().put("Message", "ObjectId does not exist").toString());
        }

        // etag check
        String planEtag = planService.getEtag(intervalKey, "eTag");

        if (ifNoneMatch != null && !ifNoneMatch.equals(planEtag)) {
            logger.info("PATCH PLAN CONFLICT");

            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        }


        // update plan
        JSONObject patchPlanJson = new JSONObject(patchPlan);

        planService.update(intervalKey, patchPlanJson);
        logger.info("PATCH PLAN : " + intervalKey + " updates successfully");

        return ResponseEntity.ok().eTag(planEtag).body(new JSONObject().put("Message ", "Updated successfully").toString());
    }

    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.PUT)
    public ResponseEntity<String> updatePlan(@RequestHeader("authorization") String idToken,
                                             @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
                                             @PathVariable String object,
                                             @PathVariable String id,
                                             @RequestBody String putPlan) {
        logger.info("PUTTING PLAN");

        // authorization
        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");

            return new ResponseEntity<>(" {\"message\": \"invalid token\" }", HttpStatus.BAD_REQUEST);
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

        // check plan exist
        String intervalKey = object + "_" + id;

        if (!planService.hasKey(intervalKey)) {
            logger.info("PUT PLAN: " + intervalKey + " does not exist");

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject()
                            .put("Message", "ObjectId does not exist")
                            .toString());
        }

        // check etag
        String planEtag = planService.getEtag(intervalKey, "eTag");

        if (ifNoneMatch != null && !ifNoneMatch.equals(planEtag)) {
            logger.info("PUT PLAN CONFLICT");

            return ResponseEntity
                    .status(HttpStatus.PRECONDITION_FAILED)
                    .eTag(planEtag)
                    .build();
        }


        // delete old plan
        planService.deletePlan(intervalKey);

        // update put plan
        JSONObject putPlanJson = new JSONObject(putPlan);

        planService.savePlan(intervalKey, putPlanJson);
        logger.info("PUT PLAN: " + intervalKey + " updates successfully");

        return ResponseEntity
                .ok()
                .eTag(planEtag)
                .body(
                        new JSONObject()
                                .put("Message ", "Updated successfully")
                                .toString()
                );
    }

    @RequestMapping(value = "/{object}/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> readByKey(@RequestHeader(value = "Authorization", required = false) String idToken,
                                            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
                                            @PathVariable String object,
                                            @PathVariable String id,
                                            WebRequest webRequest) {

        logger.info("RETRIEVING REDIS DATA: " + "object - " + object +
                "; id - " + id);

        String internalKey = object + "_" + id;

        // authorize
        logger.info("AUTHORIZATION: GOOGLE_ID_TOKEN: " + idToken);

        if (!authorizationService.authorize(idToken.substring(7))) {
            logger.error("TOKEN AUTHORIZATION - google token expired");
            return new ResponseEntity<>(" {\"message\": \"invalid token\" }", HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        if (!planService.hasKey(internalKey)) {
            logger.info("OBJECT NOT FOUND - " + internalKey);

            return new ResponseEntity<>(" {\"message\": \"item not found\" }", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> foundValue = new HashMap<>();
        foundValue = planService.getPlan(internalKey);

        // e-tag
        String objectEtag = planService.getEtag(internalKey, "eTag");

        if (objectEtag.equals(ifNoneMatch)) {
            logger.info("CACHING AVAILABLE: " + internalKey);
            return new ResponseEntity<>("{}", HttpStatus.NOT_MODIFIED);
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
            return new ResponseEntity<>(" {\"message\": \"invalid token\" }", HttpStatus.BAD_REQUEST);
        }

        logger.info("TOKEN AUTHORIZATION SUCCESSFUL");

        String intervalKey = object + "_" + id;

        if (!planService.hasKey(intervalKey)) {
            return new ResponseEntity<>(" {\"message\": \"item not found\" }", HttpStatus.NOT_FOUND);
        }

        planService.deletePlan(intervalKey);
        logger.info("DELETED SUCCESSFULLY: " + object + "_" + intervalKey);

        return new ResponseEntity<>("{\"message\":" + "\"" + intervalKey + " Deleted\"}", HttpStatus.OK);

    }
}
