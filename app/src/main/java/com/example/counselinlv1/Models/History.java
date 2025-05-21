package com.example.counselinlv1.Models;

public class History {
    private String historyID;
    private String userID;
    private String type;
    private String title;
    private String date;
    private String time;
    private String details;

    // Empty constructor for Firebase
    public History() {}

    // Constructor with all fields
    public History(String historyID, String userID, String type, String title, String date, String time, String details) {
        this.historyID = historyID;
        this.userID = userID;
        this.type = type;
        this.title = title;
        this.date = date;
        this.time = time;
        this.details = details;
    }

    // Getters and setters
    public String getHistoryID() { return historyID; }
    public void setHistoryID(String historyID) { this.historyID = historyID; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
