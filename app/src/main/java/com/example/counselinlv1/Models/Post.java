package com.example.counselinlv1.Models;

public class Post {
    private String postID;
    private String caption;
    private String fileURL;
    private String postedBy;
    private String departmentID;
    private Long timestamp; // Use Long if Firestore stores it as a number

    // Default constructor (required for Firestore)
    public Post() {}

    // Getters and setters
    public String getPostID() { return postID; }
    public void setPostID(String postID) { this.postID = postID; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public String getFileURL() { return fileURL; }
    public void setFileURL(String fileURL) { this.fileURL = fileURL; }

    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }

    public String getDepartmentID() { return departmentID; }
    public void setDepartmentID(String departmentID) { this.departmentID = departmentID; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
