package com.example.counselinlv1.Models;

public class Notification {

    private String notificationID;
    private String targetType; // e.g., "department", "user"
    private String targetID;   // The ID of the department or user this notification is for
    private String title;      // Title of the notification (e.g., "New Post")
    private String message;    // Message content of the notification
    private String type;       // Type of notification (e.g., "post", "referral")
    private long timestamp;    // Timestamp of when the notification was created
    private String postedBy;   // The user ID of the person who created this notification

    // Default constructor (required for Firestore)
    public Notification() {
    }

    // Constructor with all fields
    public Notification(String notificationID, String targetType, String targetID, String title, String message, String type, long timestamp, String postedBy) {
        this.notificationID = notificationID;
        this.targetType = targetType;
        this.targetID = targetID;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.postedBy = postedBy;
    }

    // Getters and setters
    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetID() {
        return targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }
}
