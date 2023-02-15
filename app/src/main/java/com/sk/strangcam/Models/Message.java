package com.sk.strangcam.Models;

public class Message {
    private String messageId, message, senderId;
    private long timestarmp;

    public Message() {
    }

    public Message(String messageId, String message, String senderId, long timestarmp) {
        this.messageId = messageId;
        this.message = message;
        this.senderId = senderId;
        this.timestarmp = timestarmp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestarmp() {
        return timestarmp;
    }

    public void setTimestarmp(long timestarmp) {
        this.timestarmp = timestarmp;
    }
}
