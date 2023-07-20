package com.example.whatsapp.Models;

public class MessageModels {
    String MessageId , Uid , message , ImageUrl , time , Date;
    Long timeStamp;

    public MessageModels(String uid, String message, String imageUrl, String time, Long timeStamp , String Date) {
        Uid = uid;
        this.message = message;
        ImageUrl = imageUrl;
        this.time = time;
        this.timeStamp = timeStamp;
        this.Date = Date;
    }

    public MessageModels(String uid, String message) {
        Uid = uid;
        this.message = message;
    }

    public MessageModels() {
    }

    public String getMessageId() {
        return MessageId;
    }

    public void setMessageId(String messageId) {
        MessageId = messageId;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
