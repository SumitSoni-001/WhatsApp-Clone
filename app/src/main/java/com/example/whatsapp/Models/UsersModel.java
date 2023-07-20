package com.example.whatsapp.Models;

// This is the Model class
public class UsersModel {
    String profilePic , username , email , password , userId , lastMsg , about , Phone , TimeStamp ;

    // Constructors
    public UsersModel(String profilePic, String username, String email, String password, String userId,
                      String lastMsg, String about, String phone, String timeStamp) {
        this.profilePic = profilePic;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.lastMsg = lastMsg;
        this.about = about;
        Phone = phone;
        TimeStamp = timeStamp;
    }

    // Empty Constructor :- Necessary
    public UsersModel() {}

    // Email/Password Authentication Constructor
    public UsersModel(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Phone Authentication Constructor
    public UsersModel(String phone , String username) {
        Phone = phone;
        this.username = username;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }
}
