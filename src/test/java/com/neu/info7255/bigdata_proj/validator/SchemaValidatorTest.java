package com.neu.info7255.bigdata_proj.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
class SchemaValidatorTest {

    private String testData = "{\"hello\" : 123}";

    @Test
    void validateSchema() {
        SchemaValidator schemaValidator = new SchemaValidator();

        try {
            System.out.println(new JSONObject(testData).toString());
            schemaValidator.validateSchema(new JSONObject(testData));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}