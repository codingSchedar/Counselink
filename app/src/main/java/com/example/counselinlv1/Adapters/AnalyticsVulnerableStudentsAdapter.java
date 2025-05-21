package com.example.counselinlv1.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counselinlv1.R;
import com.example.counselinlv1.Utilities.StudentScore;

import java.util.List;

public class AnalyticsVulnerableStudentsAdapter extends RecyclerView.Adapter<AnalyticsVulnerableStudentsAdapter.StudentViewHolder> {

    private List<StudentScore> students;

    public AnalyticsVulnerableStudentsAdapter(List<StudentScore> students) {
        this.students = students;
    }

    public void updateData(List<StudentScore> newStudents) {
        this.students = newStudents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vulnerable_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentScore student = students.get(position);
        holder.studentName.setText(student.getStudentName());
        holder.vulnerabilityScore.setText(String.format("%.2f", student.getScore()));
    }

    @Override
    public int getItemCount() {
        return students != null ? students.size() : 0;
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, vulnerabilityScore;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            vulnerabilityScore = itemView.findViewById(R.id.vulnerabilityScore);
        }
    }

}

