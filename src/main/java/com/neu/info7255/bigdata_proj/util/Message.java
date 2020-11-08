package com.neu.info7255.bigdata_proj.util;

public abstract class Message {

    private static String msgHeader;

    private static String msgBody;

    public Message() {

    }

    public Message(String msgHeader, String msgBody) {
        this.msgHeader = msgHeader;
        this.msgBody = msgBody;
    }

    public String getMsgHeader() {
        return msgHeader;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgHeader(String msgHeader) {
        this.msgHeader = msgHeader;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }
}
