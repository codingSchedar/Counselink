package com.example.counselinlv1.Models;

public class Appointment {
    private String appointmentID;
    private String userID;
    private String date;
    private String time;

    // Default constructor required for calls to DataSnapshot.getValue(Appointment.class)
    public Appointment() {
    }

    public Appointment(String appointmentID, String userID, String date, String time) {
        this.appointmentID = appointmentID;
        this.userID = userID;
        this.date = date;
        this.time = time;
    }

    // Getters and Setters
    public String getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
