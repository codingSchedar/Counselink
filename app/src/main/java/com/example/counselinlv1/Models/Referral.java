package com.example.counselinlv1.Models;

public class Referral {
    private String id;
    private String studentID;
    private String referrerID;  // Field for referrer ID
    private String studentName;
    private String academicReason;
    private String socialEmotionalReason;
    private String comments;
    private String status;
    private long createdAt;
    private long updatedAt;

    // No-argument constructor for Firestore deserialization
    public Referral() {}

    // Constructor with all fields, including referrerID
    public Referral(String id, String studentID, String referrerID, String studentName,
                    String academicReason, String socialEmotionalReason, String comments,
                    String status, long createdAt, long updatedAt) {
        this.id = id;
        this.studentID = studentID;
        this.referrerID = referrerID;
        this.studentName = studentName;
        this.academicReason = academicReason;
        this.socialEmotionalReason = socialEmotionalReason;
        this.comments = comments;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters for all fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getReferrerID() {
        return referrerID;
    }

    public void setReferrerID(String referrerID) {
        this.referrerID = referrerID;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getAcademicReason() {
        return academicReason;
    }

    public void setAcademicReason(String academicReason) {
        this.academicReason = academicReason;
    }

    public String getSocialEmotionalReason() {
        return socialEmotionalReason;
    }

    public void setSocialEmotionalReason(String socialEmotionalReason) {
        this.socialEmotionalReason = socialEmotionalReason;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
