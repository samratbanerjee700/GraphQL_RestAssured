package com.mutations;

import java.util.UUID;

public class User {

    private UUID id;
    private String name;
    private String rocket;
    private String twitter;

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public User(UUID id, String name, String rocket, String twitter) {
        this.id = id;
        this.name = name;
        this.rocket = rocket;
        this.twitter = twitter;
    }
    public User(UUID id, String name, String rocket) {
        this.id = id;
        this.name = name;
        this.rocket = rocket;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRocket() {
        return rocket;
    }

    public void setRocket(String rocket) {
        this.rocket = rocket;
    }
}
