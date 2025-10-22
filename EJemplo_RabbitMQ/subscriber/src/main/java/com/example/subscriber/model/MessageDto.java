package com.example.subscriber.model;

import java.io.Serializable;

public class MessageDto implements Serializable {
    private String content;
    private String sender;
    
    public MessageDto() {}
    
    public MessageDto(String content, String sender) {
        this.content = content;
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    @Override
    public String toString() {
        return "MessageDto{content='" + content + "', sender='" + sender + "'}";
    }
}
