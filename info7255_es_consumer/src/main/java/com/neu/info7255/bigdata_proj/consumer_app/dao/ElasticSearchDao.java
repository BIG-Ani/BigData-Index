package com.neu.info7255.bigdata_proj.consumer_app.dao;

import com.neu.info7255.bigdata_proj.consumer_app.constant.Constant;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class ElasticSearchDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestHighLevelClient client;

    public String postDocument(JSONObject plan) throws IOException {
        if(!indexExist()) {
            createElasticIndex();
        }

        IndexRequest request = new IndexRequest(Constant.PLAN);
        request.source(plan.toString(), XContentType.JSON);
        request.id(plan.get("objectId").toString());

        if (plan.has("parent_id")) {
            request.routing(plan.get("parent_id").toString());
        }

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

        logger.info("response id: " + indexResponse.getId());
        return indexResponse.getResult().name();
    }

    public String postPlanDoc(String id, JSONObject plan) throws IOException {
        if (indexExist()) {
            logger.info("plan index already exist in es");
        } else {
            createElasticIndex();
        }

        logger.info("ELASTICSEARCH CREATE: create the plan index with PlanId - " + id);
        IndexRequest request = new IndexRequest(Constant.PLAN)
                .id(id);

        if (plan.has("parent_id")) {
            request.routing(plan.get("parent_id").toString());
        }

        client.index(request, RequestOptions.DEFAULT);

        IndexResponse indexResp = null;
        try {
            indexResp = client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.info("ES CREATE DOC ERROR:" + e.getMessage());
            e.printStackTrace();
        }

        return indexResp.status().toString();
    }

    private void createElasticIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(Constant.PLAN);
        request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 2));
        String mapping = getMapping();
        request.mapping(mapping, XContentType.JSON);

        client.indices().create(request, RequestOptions.DEFAULT);
    }

    private String getMapping() {
        String mapping = "{\r\n" +
                "    \"properties\": {\r\n" +
                "      \"objectId\": {\r\n" +
                "        \"type\": \"keyword\"\r\n" +
                "      },\r\n" +
                "      \"plan_service\":{\r\n" +
                "        \"type\": \"join\",\r\n" +
                "        \"relations\":{\r\n" +
                "          \"plan\": [\"membercostshare\", \"planservice\"],\r\n" +
                "          \"planservice\": [\"service\", \"planservice_membercostshare\"]\r\n" +
                "        }\r\n" +
                "      }\r\n" +
                "    }\r\n" +
                "  }\r\n" +
                "}";

        return mapping;
    }

    public boolean indexExist() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(Constant.PLAN);

        return client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    public String getPlanDoc(String id) {

        logger.info("ELASTICSEARCH READ: read the plan index with PlanId - " + id);

        GetRequest getRequest = new GetRequest(
                Constant.PLAN,
                id);

        GetResponse planDoc = null;
        try {
            planDoc = client.get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("ELASTICSEARCH READ: " + e.getMessage());

            e.printStackTrace();
        }

        String plan = "";
        if (!planDoc.isExists()) {
            logger.error("ES CREATE DOC ERROR: Item not found");
        } else {

            plan = planDoc.getSourceAsString();
        }

        return plan;
    }

    public Boolean updatePlanDoc(String id, JSONObject updatePlan) {
        logger.info("ELASTICSEARCH UPDATE: read the plan index with PlanId - " + id);

        UpdateRequest request = new UpdateRequest(
                Constant.PLAN,
                id).doc(updatePlan.toString(), XContentType.JSON);;

        UpdateResponse update = null;
        try {
            update = client.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("ELASTICSEARCH UPDATE: read the plan index with PlanId - " + id);

            e.printStackTrace();
        }

        return update.status().equals(RestStatus.OK);
    }

    public String deletePlanDoc(String id) {

        logger.info("ELASTICSEARCH DELETE: delete the plan index with PlanId - " + id);

        DeleteRequest request = new DeleteRequest(
                Constant.PLAN,
                id);

        DeleteResponse delete = null;
        try {
            delete = client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("ELASTICSEARCH DELETE: fail to delete the plan index with PlanId - " + id);

            e.printStackTrace();
        }

        if (delete == null) {
            logger.error("ELASTICSEARCH DELETE: item not found");
            return "item not found";
        } else {
            logger.info("ELASTICSEARCH DELETE: successfully delete plan with id -" + id);

            return delete.getResult().name();
        }
    }
}
