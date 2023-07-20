package com.example.whatsapp.Models;

// This model is for firebase , it wil store the status(Image) url and upload time.
public class StatusModel {

    String ImageUrl , currentTime;
    Long uploadTime;

    public StatusModel(String imageUrl, String currentTime, Long uploadTime) {
        ImageUrl = imageUrl;
        this.currentTime = currentTime;
        this.uploadTime = uploadTime;
    }

    public StatusModel() {
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }
}
