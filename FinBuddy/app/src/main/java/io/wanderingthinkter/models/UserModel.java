package io.wanderingthinkter.models;

import com.google.firebase.Timestamp;

public class UserModel {
    private String name;
    private String email;
    private String profilePic;
    private Timestamp joinedDate;

    public UserModel() {
    }

    public UserModel(String name, String email, Timestamp joinedDate) {
        this.name = name;
        this.email = email;
        this.joinedDate = joinedDate;
    }

    public UserModel(String name, String email, String profilePic, Timestamp joinedDate) {
        this.name = name;
        this.email = email;
        this.profilePic = profilePic;
        this.joinedDate = joinedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Timestamp getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(Timestamp joinedDate) {
        this.joinedDate = joinedDate;
    }
}
