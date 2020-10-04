package com.neu.info7255.bigdata_proj.validator;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class SchemaValidator {

    private static final Logger logger = LoggerFactory.getLogger(SchemaValidator.class);

    private String jsonPath = "/jsonSchema.json";

    public void validateSchema(JSONObject data) throws ValidationException {
        logger.info("SCHEMA VALIDATING: schema path-" + jsonPath + ": data-" + data.toString());

        InputStream inputStream = getClass().getResourceAsStream(jsonPath);
        JSONObject schemaJson = new JSONObject(new JSONTokener(inputStream));
        Schema schema = SchemaLoader.load(schemaJson);

        schema.validate(data);
    }

}
