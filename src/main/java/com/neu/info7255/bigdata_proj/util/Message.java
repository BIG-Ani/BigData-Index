package com.neu.info7255.bigdata_proj.util;

public abstract class Message {

    private String msgHeader;

    private String msgBody;

    public void setMsgHeader(String msgHeader) {
        this.msgHeader = msgHeader;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }
}
