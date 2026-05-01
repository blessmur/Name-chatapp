package com.example.chatapp;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String region;

    public User() {
    }

    public User(String username, String region) {
        this.username = username;
        this.region = region;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRegion() {
        return region;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}