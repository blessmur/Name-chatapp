package com.example.chatapp;

public class ChatMessage {

    private String username;
    private String content;
    private String region;
    private String type;

    public ChatMessage() {
    }

    public ChatMessage(String username, String content, String region, String type) {
        this.username = username;
        this.content = content;
        this.region = region;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}