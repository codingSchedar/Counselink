package com.example.counselinlv1.Utilities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.counselinlv1.Models.Referral;
import com.example.counselinlv1.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReferralDetailsDialog extends Dialog {

    private Referral referral;
    private FirebaseFirestore firestore;
    private OnStatusUpdatedListener listener;

    private String userType;
    private String currentUserId;
    private String departmentID;

    private TextView studentNameText, studentIdText, referralDateText, academicConcernText, socialEmotionalConcernText, commentsText, referrerNameText;
    private Spinner statusSpinner;
    private Button updateButton, deleteButton;

    @FunctionalInterface
    public interface OnStatusUpdatedListener {
        void onStatusUpdated(String newStatus);
    }

    public ReferralDetailsDialog(@NonNull Context context, Referral referral, String userType, String currentUserId, String departmentID, OnStatusUpdatedListener listener) {
        super(context);
        this.referral = referral;
        this.userType = userType;
        this.currentUserId = currentUserId;
        this.departmentID = departmentID;
        this.listener = listener;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_referral_details);

        // Initialize views
        studentNameText = findViewById(R.id.student_name);
        studentIdText = findViewById(R.id.student_id);
        referralDateText = findViewById(R.id.referral_date);
        academicConcernText = findViewById(R.id.academic_concern_text);
        socialEmotionalConcernText = findViewById(R.id.social_emtional_concern_text);
        commentsText = findViewById(R.id.comments_text);
        referrerNameText = findViewById(R.id.referrer_name);
        statusSpinner = findViewById(R.id.status_spinner);
        updateButton = findViewById(R.id.update_button);
        deleteButton = findViewById(R.id.delete_button);

        // Populate fields
        studentNameText.setText(referral.getStudentName());
        studentIdText.setText(referral.getStudentID());
        referralDateText.setText(formatDate(referral.getCreatedAt()));
        academicConcernText.setText(referral.getAcademicReason());
        socialEmotionalConcernText.setText(referral.getSocialEmotionalReason());
        commentsText.setText(referral.getComments());

        // Retrieve and display the referrer's name
        fetchReferrerName(referral.getReferrerID());

        // Set up status spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.referral_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Set the current status in spinner
        if (referral.getStatus() != null) {
            int position = statusAdapter.getPosition(referral.getStatus());
            statusSpinner.setSelection(position);
        }

        // Adjust spinner accessibility and button visibility
        checkUserRoleAndSetSpinnerAccessibility();

        // Update button click listener
        updateButton.setOnClickListener(v -> updateReferralStatus());
        // Delete button click listener
        deleteButton.setOnClickListener(v -> deleteReferral());
    }

    private void deleteReferral() {
        // Show a confirmation dialog
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Referral")
                .setMessage("Are you sure you want to delete this referral?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Validate referral ID
                    if (referral.getId() == null || referral.getId().isEmpty()) {
                        Toast.makeText(getContext(), "Invalid referral ID. Cannot delete.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Proceed to delete the referral from Firestore
                    firestore.collection("Referrals")
                            .document(referral.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Referral deleted successfully", Toast.LENGTH_SHORT).show();

                                // Notify the adapter (if listener is set)
                                if (listener != null) {
                                    listener.onStatusUpdated("deleted");
                                }

                                dismiss(); // Close the dialog
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete referral: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace(); // Log the error for debugging
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User cancelled the deletion
                    dialog.dismiss();
                })
                .create()
                .show();
    }



    private void fetchReferrerName(String referrerID) {
        if (referrerID != null && !referrerID.isEmpty()) {
            firestore.collection("Users").document(referrerID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            String referrerName = document.getString("firstName") + " " + document.getString("lastName");
                            referrerNameText.setText(referrerName != null ? referrerName : "Unknown Referrer");
                        } else {
                            referrerNameText.setText("Unknown Referrer");
                            Toast.makeText(getContext(), "Failed to fetch referrer details", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            referrerNameText.setText("Unknown Referrer");
        }
    }

    private void checkUserRoleAndSetSpinnerAccessibility() {
        // Use the userType directly to determine spinner accessibility
        if ("student".equals(userType) || "faculty".equals(userType)) {
            statusSpinner.setEnabled(false); // Disable the spinner for students and faculty
            updateButton.setVisibility(Button.GONE); // Hide the button
            deleteButton.setVisibility(Button.GONE);
        } else if ("counselor".equals(userType)) {
            statusSpinner.setEnabled(true); // Enable the spinner for counselors
            updateButton.setVisibility(Button.VISIBLE);
            deleteButton.setVisibility(Button.VISIBLE);// Show the button
        } else {
            Toast.makeText(getContext(), "Unknown user role. Update functionality disabled.", Toast.LENGTH_SHORT).show();
            statusSpinner.setEnabled(false);
            updateButton.setVisibility(Button.GONE);
        }
    }

    private void updateReferralStatus() {
        String newStatus = statusSpinner.getSelectedItem().toString();

        // Validate referral ID
        if (referral.getId() == null || referral.getId().isEmpty()) {
            Toast.makeText(getContext(), "Invalid referral ID. Cannot update status.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore
        firestore.collection("Referrals")
                .document(referral.getId()) // Use the document ID to target the specific referral
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();

                    // Notify the listener to refresh the RecyclerView
                    if (listener != null) {
                        listener.onStatusUpdated(newStatus);
                    }

                    // Create notification for the referrer
                    createReferralStatusNotification(newStatus);

                    // Close the dialog
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace(); // Log the error for debugging
                });
    }

    private void createReferralStatusNotification(String newStatus) {
        String notificationID = firestore.collection("Notifications").document().getId();

        // Fetch referral details for a descriptive message
        firestore.collection("Referrals")
                .document(referral.getId()) // Use the referral's ID to get details
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String studentName = documentSnapshot.getString("studentName");
                        long createdAt = documentSnapshot.getLong("createdAt");

                        // Format the referral creation date
                        String formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(new Date(createdAt));

                        // Create the notification message
                        String message = "The status of your referral about " + studentName + " on " + formattedDate + " has been updated to: " + newStatus;

                        // Create the notification map
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("notificationID", notificationID);
                        notification.put("targetType", "individual");
                        notification.put("targetID", referral.getReferrerID()); // Notify the referrer
                        notification.put("title", "Referral Status Updated");
                        notification.put("message", message);
                        notification.put("type", "referral_status");
                        notification.put("timestamp", System.currentTimeMillis()); // Store as long

                        firestore.collection("Notifications")
                                .document(notificationID)
                                .set(notification)
                                .addOnSuccessListener(aVoid -> {
                                    // Notification created successfully
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to create notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "Referral details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching referral details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private String formatDate(long timestamp) {
        return android.text.format.DateFormat.format("MM/dd/yyyy", new java.util.Date(timestamp)).toString();
    }
}
