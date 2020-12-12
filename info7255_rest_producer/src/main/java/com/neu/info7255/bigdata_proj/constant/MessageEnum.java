package com.neu.info7255.bigdata_proj.constant;

public enum MessageEnum {

    PATCH_SUCCESS("update success", "patch update successfully"),
    PUT_SUCCESS("update success", "put update successfully"),
    GET_SUCCESS("find success", "item found"),
    SAVE_SUCCESS("save success", "item saved"),
    DELETE_SUCCESS("delete success", "item deleted"),

    IF_MATCH_MISSING_ERROR("If-match missing error", "header If_match missing"),
    IF_MATCH_ERROR("If-match error", "header If_match does not match"),
    VALIDATION_ERROR("validation error", "json schema wrong"),
    CONFLICT_ERROR("conflict error", "item already exist"),
    NOT_FOUND_ERROR("missing error", "item not found"),
    AUTHORIZATION_ERROR("authorization error", "invalid token")
    ;

    private String header;
    private String body;

    MessageEnum(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return header + ":" + body;
    }
}
