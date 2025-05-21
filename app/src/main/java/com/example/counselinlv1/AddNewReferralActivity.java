package com.example.counselinlv1;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.counselinlv1.Utilities.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddNewReferralActivity extends AppCompatActivity {

    // Declare UI components
    private Spinner academicReasonSpinner, socialEmotionalReasonSpinner;
    private EditText studentNameField, studentNumberField;
    private EditText academicOtherField, socialEmotionalOtherField;
    private EditText commentsField;
    private Button saveButton;
    private String referrerID;

    // Firestore and FirebaseAuth instances
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_referral);

        // Initialize Firestore and FirebaseAuth
        firestore = FirebaseFirestore.getInstance();
        // Retrieve the user ID passed from HomeScreenActivity
        referrerID = getIntent().getStringExtra("userID");

        // Validate the user ID
        if (TextUtils.isEmpty(referrerID)) {
            Toast.makeText(this, "Error: User ID is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        academicReasonSpinner = findViewById(R.id.academicReasonSpinner);
        socialEmotionalReasonSpinner = findViewById(R.id.socialEmotionalReasonSpinner);
        studentNameField = findViewById(R.id.studentName);
        studentNumberField = findViewById(R.id.studentNumber);
        academicOtherField = findViewById(R.id.academicOther);
        socialEmotionalOtherField = findViewById(R.id.socialEmotionalOther);
        commentsField = findViewById(R.id.commentsField);
        saveButton = findViewById(R.id.saveButton);

        // Set up the spinners
        setupSpinners();

        // Save button listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveReferral();
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> academicReasonAdapter = ArrayAdapter.createFromResource(this,
                R.array.academic_reasons, android.R.layout.simple_spinner_item);
        academicReasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        academicReasonSpinner.setAdapter(academicReasonAdapter);

        ArrayAdapter<CharSequence> socialEmotionalReasonAdapter = ArrayAdapter.createFromResource(this,
                R.array.social_emotional_reasons, android.R.layout.simple_spinner_item);
        socialEmotionalReasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialEmotionalReasonSpinner.setAdapter(socialEmotionalReasonAdapter);
    }

    private void validateAndSaveReferral() {
        String studentName = studentNameField.getText().toString().trim();
        String studentNumber = studentNumberField.getText().toString().trim();
        String academicReason = academicReasonSpinner.getSelectedItem().toString();
        String socialEmotionalReason = socialEmotionalReasonSpinner.getSelectedItem().toString();
        String academicOther = academicOtherField.getText().toString().trim();
        String socialEmotionalOther = socialEmotionalOtherField.getText().toString().trim();
        String comments = commentsField.getText().toString().trim();

        if (TextUtils.isEmpty(studentNumber)) {
            Toast.makeText(this, "Student number is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (academicReason.equals("NA") && socialEmotionalReason.equals("NA")) {
            Toast.makeText(this, "At least one reason must be selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (academicReason.equals("Others") && TextUtils.isEmpty(academicOther)) {
            Toast.makeText(this, "Specify the academic reason", Toast.LENGTH_SHORT).show();
            return;
        }
        if (socialEmotionalReason.equals("Others") && TextUtils.isEmpty(socialEmotionalOther)) {
            Toast.makeText(this, "Specify the social/emotional reason", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(comments)) {
            Toast.makeText(this, "Comments are required", Toast.LENGTH_SHORT).show();
            return;
        }

        validateStudentNumber(studentNumber, new StudentValidationCallback() {
            @Override
            public void onValid() {
                saveReferralToFirestore(studentNumber, academicReason, socialEmotionalReason,
                        academicOther, socialEmotionalOther, comments);
            }

            @Override
            public void onInvalid() {
                Toast.makeText(AddNewReferralActivity.this, "Invalid student number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateStudentNumber(String studentNumber, StudentValidationCallback callback) {
        firestore.collection("Users")
                .document(studentNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            callback.onValid();
                        } else {
                            callback.onInvalid();
                        }
                    }
                });
    }

    private void saveReferralToFirestore(String studentNumber, String academicReason,
                                         String socialEmotionalReason, String academicOther,
                                         String socialEmotionalOther, String comments) {

        // Access the Users table to verify the student name
        firestore.collection("Users")
                .document(studentNumber)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the student name from the database
                        String verifiedStudentName = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");

                        // Create a map for the referral data
                        Map<String, Object> referral = new HashMap<>();
                        referral.put("studentName", verifiedStudentName); // Use verified name
                        referral.put("studentID", studentNumber);
                        referral.put("academicReason", academicReason.equals("Others") ? academicOther : academicReason);
                        referral.put("socialEmotionalReason", socialEmotionalReason.equals("Others") ? socialEmotionalOther : socialEmotionalReason);
                        referral.put("comments", comments);
                        referral.put("status", "Pending");
                        referral.put("createdAt", System.currentTimeMillis());
                        referral.put("updatedAt", System.currentTimeMillis());
                        referral.put("referrerID", referrerID); // Add the referrerID to the referral

                        // Save the referral to Firestore
                        firestore.collection("Referrals")
                                .add(referral)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Referral saved successfully", Toast.LENGTH_SHORT).show();
                                    createNotification(verifiedStudentName, comments, studentNumber);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save referral: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Handle case where the student number does not exist
                        Toast.makeText(this, "Invalid student number. No user found with this ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors during the database lookup
                    Toast.makeText(this, "Error verifying student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createNotification(String studentName, String comments, String studentID) {
        // Query Firestore to fetch the referrer's name using the referrerID
        firestore.collection("Users")
                .document(referrerID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the referrer's name from the document
                        String referrerName = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");

                        // Proceed to create the notification with the referrer's name
                        String notificationID = firestore.collection("Notifications").document().getId();

                        Map<String, Object> notification = new HashMap<>();
                        notification.put("notificationID", notificationID);
                        notification.put("targetType", "individual");
                        notification.put("targetID", studentID);
                        notification.put("title", "New Referral");
                        notification.put("message", "You have been referred by " + referrerName + ".\nComment: " + comments);
                        notification.put("type", "referral");
                        notification.put("timestamp", System.currentTimeMillis()); // Store as long

                        firestore.collection("Notifications")
                                .document(notificationID)
                                .set(notification)
                                .addOnSuccessListener(aVoid -> {
                                    // Notification created successfully
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to create notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Referrer not found in the database.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {

                });
    }



    interface StudentValidationCallback {
        void onValid();
        void onInvalid();
    }


}
