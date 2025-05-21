package com.example.counselinlv1.Utilities;


public class StudentScore {
    private String studentName;
    private double score;

    public StudentScore(String studentName, double score) {
        this.studentName = studentName;
        this.score = score;
    }

    public String getStudentName() {
        return studentName;
    }

    public double getScore() {
        return score;
    }
}
