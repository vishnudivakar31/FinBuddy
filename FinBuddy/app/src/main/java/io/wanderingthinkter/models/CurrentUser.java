package io.wanderingthinkter.models;

public class CurrentUser {
    private String userId;
    private String username;

    private static CurrentUser instance;

    private CurrentUser() {
    }

    public static CurrentUser getInstance() {
        if(instance == null) instance = new CurrentUser();
        return instance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
