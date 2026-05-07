package com.example.chatapp;

public class AuthResponse {

    private boolean success;
    private String message;
    private String username;
    private String region;
    private String city;
    private Double latitude;
    private Double longitude;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.username = user.getUsername();
        this.region = user.getRegion();
        this.city = user.getCity();
        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}