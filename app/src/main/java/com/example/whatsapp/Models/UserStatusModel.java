package com.example.whatsapp.Models;

import java.util.ArrayList;

public class UserStatusModel {

    // The below 2 type of variables are used for information seen when we click on any status.
    String username , profilePic;
    Long lastUpdated;

    private ArrayList<StatusModel> statuses;    // It is used to store the URL of the statuses that the user upload.

    public UserStatusModel(String username, String profilePic,Long lastUpdated, ArrayList<StatusModel> statuses) {
        this.username = username;
        this.profilePic = profilePic;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }

    public UserStatusModel() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArrayList<StatusModel> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<StatusModel> statuses) {
        this.statuses = statuses;
    }
}
