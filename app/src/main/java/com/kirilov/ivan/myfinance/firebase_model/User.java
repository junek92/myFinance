package com.kirilov.ivan.myfinance.firebase_model;

/**
 * Created by Ivan on 27-May-16.
 */
public class User {
    private String userEmail;
    private long userJoined;
    private String userName;
    private long userCurrentMonth;

    public User() {
    }

    public User(String userEmail, long userJoined, String userName, long userCurrentMonth) {
        this.userEmail = userEmail;
        this.userJoined = userJoined;
        this.userName = userName;
        this.userCurrentMonth = userCurrentMonth;
    }

    // GETTERS
    public String getUserEmail() {
        return userEmail;
    }

    public long getUserJoined() {
        return userJoined;
    }

    public String getUserName() {
        return userName;
    }

    public long getUserCurrentMonth() {
        return userCurrentMonth;
    }

    // SETTERS
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserJoined(long userJoined) {
        this.userJoined = userJoined;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserCurrentMonth(long userCurrentMonth) {
        this.userCurrentMonth = userCurrentMonth;
    }
}
