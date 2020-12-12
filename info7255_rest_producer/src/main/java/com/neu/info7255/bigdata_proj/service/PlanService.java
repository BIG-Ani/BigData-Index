package com.neu.info7255.bigdata_proj.service;

import com.neu.info7255.bigdata_proj.constant.Constant;
import com.neu.info7255.bigdata_proj.dao.RedisDao;
import com.neu.info7255.bigdata_proj.util.MessageDigestGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlanService implements RedisService {

    private static String SPLITTER_UNDER_SLASH = "_";

    private static Logger logger = LoggerFactory.getLogger(PlanService.class);

    private Map<String,String> relationMap = new HashMap<>();

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private KafkaPub kafkaPub;

    @Override
    public void create(String key, String value) {
        logger.info("CREATING NEW DATA: [" + key + " - " + value + "]");
        redisDao.putValue(key, value);
    }

    @Override
    public boolean hasKey(String key) {
        return redisDao.hasKey(key);
    }

    @Override
    public void deletePlan(String key) {
        populate(key, null, true);
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

    @Override
    public String savePlan(String key, JSONObject object) {
        // save plan
        Map<String, Object> objectMap = nestStore(key, object);

        indexQueue(object, object.getString("objectId"));

        // set new etag
//        String newEtag = MessageDigestGenerator.getSequence(MessageDigestGenerator.MD5_ALGORITHM, key);

        String newEtag = MessageDigestGenerator.md5Gen(key);

        redisDao.hSet(key, "eTag", newEtag);

        return newEtag;
    }

    private Map<String, Object> nestStore(String key, JSONObject object) {

        traverseNode(object);

        Map<String, Object> output = new HashMap<>();
        populate(key, output, false);

        return output;
    }

    // store the nested json object
    public Map<String, Map<String, Object>> traverseNode(JSONObject jsonObject) {
        Map<String, Map<String, Object>> objMap = new HashMap<>();
        Map<String, Object> valueMap = new HashMap<>();

        // get all attributes
        Iterator<String> keys = jsonObject.keySet().iterator();
        logger.info(jsonObject.toString() + "'s ALL ATTRIBUTES: " + jsonObject.keySet().toString());

        // traverse all attributes for store
        while (keys.hasNext()) {

            String objectKey = jsonObject.getString("objectType") + "_" + jsonObject.getString("objectId");

            String attName = keys.next();
            Object attValue = jsonObject.get(attName);

            // type - Object
            if (attValue instanceof JSONObject) {

                attValue = traverseNode((JSONObject) attValue);

                Map<String, Map<String, Object>> ObjValueMap = (HashMap<String, Map<String, Object>>) attValue;

                String transitiveKey = objectKey + "_" + attName;
                redisDao.setAdd(transitiveKey, ObjValueMap.entrySet().iterator().next().getKey());
            }
            else if (attValue instanceof JSONArray) {

                // type - Array
                attValue = getNodeList((JSONArray)attValue);

                List<HashMap<String, HashMap<String, Object>>> formatList = (List<HashMap<String, HashMap<String, Object>>>) attValue;
                formatList.forEach((listObject) -> {

                    listObject.entrySet().forEach((listEntry) -> {

                        String internalKey = objectKey + "_" + attName;

                        redisDao.setAdd(internalKey, listEntry.getKey());

                    });

                });
            } else {
                // type - Object
                redisDao.hSet(objectKey, attName, attValue.toString());

                valueMap.put(attName, attValue);
                objMap.put(objectKey, valueMap);
            }

        }

        return objMap;
    }

    private void indexQueue(JSONObject jsonObject, String uuid) {

        Map<String, String> simpleMap = new HashMap<>();

        for (Object key : jsonObject.keySet()) {
            String attributeKey = String.valueOf(key);
            Object attributeVal = jsonObject.get(String.valueOf(key));
            String edge = attributeKey;

            if (attributeVal instanceof JSONObject) {
                JSONObject embdObject = (JSONObject) attributeVal;

                JSONObject joinObj = new JSONObject();
                if (edge.equals("planserviceCostShares") && embdObject.getString("objectType").equals("membercostshare")) {
                    joinObj.put("name", "planservice_membercostshare");
                } else {
                    joinObj.put("name", embdObject.getString("objectType"));
                }

                joinObj.put("parent", uuid);
                embdObject.put("plan_service", joinObj);
                embdObject.put("parent_id", uuid);
                System.out.println(embdObject.toString());
//                    messageQueueService.addToMessageQueue(embdObject.toString(), false);
                kafkaPub.publish(Constant.ES_POST, embdObject.toString());

            } else if (attributeVal instanceof JSONArray) {

                JSONArray jsonArray = (JSONArray) attributeVal;
                Iterator<Object> jsonIterator = jsonArray.iterator();

                while (jsonIterator.hasNext()) {
                    JSONObject embdObject = (JSONObject) jsonIterator.next();
                    embdObject.put("parent_id", uuid);
                    System.out.println(embdObject.toString());

                    String embd_uuid = embdObject.getString("objectId");
                    relationMap.put(embd_uuid, uuid);

                    indexQueue(embdObject, embd_uuid);
                }

            } else {
                simpleMap.put(attributeKey, String.valueOf(attributeVal));
            }
        }

        JSONObject joinObj = new JSONObject();
        joinObj.put("name", simpleMap.get("objectType"));

        if (!simpleMap.containsKey("planType")) {
            joinObj.put("parent", relationMap.get(uuid));
        }

        JSONObject obj1 = new JSONObject(simpleMap);
        obj1.put("plan_service", joinObj);
        obj1.put("parent_id", relationMap.get(uuid));
        System.out.println(obj1.toString());
//            messageQueueService.addToMessageQueue(obj1.toString(), false);
        kafkaPub.publish(Constant.ES_POST, obj1.toString());

    }


    private List<Object> getNodeList(JSONArray attValue) {

        List<Object> list = new ArrayList<>();

        if (attValue == null)   return list;

        attValue.forEach((e) -> {

            if (e instanceof JSONObject) {
                e = traverseNode((JSONObject )e);
            } else if (e instanceof JSONArray) {
                e = getNodeList((JSONArray)e);
            }

            list.add(e);

        });

        return list;
    }

    public Map<String, Object> getPlan(String key) {
        Map<String, Object> output = new HashMap<>();

        populate(key, output, false);

        return output;
    }

    @Override
    public void update(String key, JSONObject object) {
        traverseNode(object);
    }

    @Override
    public void update(JSONObject object) {
        traverseNode(object);
    }

    @Override
    public String getEtag(String key, String etag) {
        return redisDao.hGet(key, etag);
    }

    // populate plan nested node
    public Map<String, Object> populate(String objectKey, Map<String, Object> map, boolean delete) {

        // get all attributes
        Set<String> keys = redisDao.keys(objectKey + "*");

        keys.forEach((key) -> {
            // DEBUG leichenzhou - 11/7/20 bug (1)"membercostshare_1234512xvc1314sdfsd-506da" (2) "membercostshare_1234512xvc1314sdfsd-506"
            // will have operation error with objectKey(membercostshare_1234512xvc1314sdfsd-506), due to pattern reg

            if (key.length() > objectKey.length() && key.substring(objectKey.length()).indexOf(SPLITTER_UNDER_SLASH) == -1) {
                return;
            }

            // process key : value
            if (key.equals(objectKey)) {

                if (delete) {
                    redisDao.deleteKey(key);
                } else {

                    // store the string object: key-pair
                    Map<Object, Object> objMap = redisDao.hGetAll(key);

                    objMap.entrySet().forEach((att) -> {

                        String attKey = (String) att.getKey();

                        if (!attKey.equalsIgnoreCase("eTag")) {
                            String attValue = att.getValue().toString();

                            map.put(attKey, isNumberValue(attValue)? Integer.parseInt(attValue) : att.getValue());
                        }

                    });
                }
            } else {

                // nest nodes
                String subKey = key.substring((objectKey + SPLITTER_UNDER_SLASH).length());

                Set<String> objSet = redisDao.sMembers(key);

                if (objSet.size() > 1) {
                    // process nested object list
                    List<Object> objectList = new ArrayList<>();

                    objSet.forEach((member) -> {

                        if (delete) {

                            populate(member, null, delete);
                        } else {
                            Map<String, Object> listMap = new HashMap<>();

                            objectList.add(populate(member, listMap, delete));
                        }

                    });

                    if (delete) {
                        redisDao.deleteKey(key);
                    } else {
                        map.put(subKey, objectList);
                    }
                } else {
                    // process nested object

                    if (delete) {
                        redisDao.deleteKeys(Arrays.asList(key, objSet.iterator().next()));
                    } else {

                        Map<Object, Object> values = redisDao.hGetAll(objSet.iterator().next());
                        Map<String, Object> objMap = new HashMap<>();

                        values.entrySet().forEach((value) -> {

                           String name = value.getKey().toString();
                           String val = value.getValue().toString();

                           objMap.put(name, isNumberValue(val)? Integer.parseInt(val) : value.getValue());

                        });

                        map.put(subKey, objMap);

                    }

                }

            }

        });

        return map;
    }

    private boolean isNumberValue(String value) {

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            logger.info("Non-number attributes: " + e.getMessage());
            return false;
        }

    }

}