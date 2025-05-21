package com.example.counselinlv1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.counselinlv1.Models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPhone, etAddress, etStudentID;
    private Spinner departmentSpinner, programSpinner, yearLevelSpinner, genderSpinner;
    private ImageView ivProfileImage, btnClose;
    private Button btnSave;
    private String userID, currentPassword, currentUserType, currentProgram;
    private FirebaseFirestore firestore;
    private Uri selectedImageUri = null; // Store the selected image URI
    private String currentProfilePictureUrl; // Store the current profile picture URL

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firestore = FirebaseFirestore.getInstance();

        // Get userID passed from HomeScreenActivity
        userID = getIntent().getStringExtra("userID");
        if (userID == null) {
            Toast.makeText(this, "User ID is missing. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        etFirstName = findViewById(R.id.first_name);
        etLastName = findViewById(R.id.last_name);
        etEmail = findViewById(R.id.email_address);
        etPhone = findViewById(R.id.phone_number);
        etAddress = findViewById(R.id.address);
        etStudentID = findViewById(R.id.student_id);
        departmentSpinner = findViewById(R.id.department_spinner);
        programSpinner = findViewById(R.id.program_spinner);
        yearLevelSpinner = findViewById(R.id.year_level_spinner);
        genderSpinner = findViewById(R.id.gender_spinner);
        ivProfileImage = findViewById(R.id.profile_image);
        btnSave = findViewById(R.id.save_button);
        btnClose = findViewById(R.id.close_icon);

        // Populate the Department spinner
        ArrayAdapter<CharSequence> departmentAdapter = ArrayAdapter.createFromResource(
                this, R.array.department_acronyms, android.R.layout.simple_spinner_item);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpinner.setAdapter(departmentAdapter);

        // Populate the Year Level spinner
        ArrayAdapter<CharSequence> yearLevelAdapter = ArrayAdapter.createFromResource(
                this, R.array.year_level_array, android.R.layout.simple_spinner_item);
        yearLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearLevelSpinner.setAdapter(yearLevelAdapter);

        // Populate the Gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Set up department spinner listener to update the program spinner
        departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDepartment = departmentSpinner.getSelectedItem().toString();
                updateProgramSpinner(selectedDepartment, currentProgram);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        btnSave.setOnClickListener(view -> saveProfile());

        btnClose.setOnClickListener(view -> finish());

        ivProfileImage.setOnClickListener(v -> openGalleryForImage());

        loadUserProfile();
    }

    private void loadUserProfile() {
        firestore.collection("Users").document(userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                etFirstName.setText(user.getFirstName());
                                etLastName.setText(user.getLastName());
                                etEmail.setText(user.getEmail());
                                etPhone.setText(user.getPhone());
                                etAddress.setText(user.getAddress());
                                etStudentID.setText(user.getUserID());
                                currentPassword = user.getPassword();
                                currentUserType = user.getType();
                                currentProfilePictureUrl = user.getProfilePictureUrl();
                                currentProgram = user.getProgramID();

                                // Set spinners
                                setSpinnerSelection(departmentSpinner, user.getDepartmentID());
                                setSpinnerSelection(yearLevelSpinner, getYearLevelString(user.getYearLevel()));
                                setSpinnerSelection(genderSpinner, user.getGender());

                                // Update program spinner after setting department
                                updateProgramSpinner(user.getDepartmentID(), user.getProgramID());

                                // Load profile image
                                if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                                    Glide.with(this).load(user.getProfilePictureUrl()).into(ivProfileImage);
                                } else {
                                    ivProfileImage.setImageResource(R.drawable.ic_user_box); // Default image
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProgramSpinner(String department, String programToSelect) {
        int arrayId = 0;
        switch (department) {
            case "LIMA":
                arrayId = R.array.LIMA_programs;
                break;
            case "CAMP":
                arrayId = R.array.CAMP_programs;
                break;
            case "CBA":
                arrayId = R.array.CBA_programs;
                break;
            case "CCJE":
                arrayId = R.array.CCJE_programs;
                break;
            case "COD":
                arrayId = R.array.COD_programs;
                break;
            case "CCAS":
                arrayId = R.array.CCAS_programs;
                break;
            case "CON":
                arrayId = R.array.CON_programs;
                break;
            case "CITHM":
                arrayId = R.array.CITHM_programs;
                break;
            case "ETEEAP":
                arrayId = R.array.ETEEAP_programs;
                break;
            case "CTE":
                arrayId = R.array.CTE_programs;
                break;
        }

        ArrayAdapter<CharSequence> programAdapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_item);
        programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        programSpinner.setAdapter(programAdapter);

        // Set the current program in the spinner after populating it
        if (programToSelect != null) {
            setSpinnerSelection(programSpinner, programToSelect);
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner.getAdapter() == null) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0); // Default to the first item if not found
    }

    private String getYearLevelString(int yearLevel) {
        switch (yearLevel) {
            case 1:
                return "1st Year";
            case 2:
                return "2nd Year";
            case 3:
                return "3rd Year";
            case 4:
                return "4th Year";
            case 5:
                return "5th Year";
            case 6:
                return "6th Year";
            default:
                return "Unknown Year";
        }
    }

    private void openGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData(); // Store the selected image URI
            ivProfileImage.setImageURI(selectedImageUri); // Update UI immediately
        }
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String departmentID = departmentSpinner.getSelectedItem().toString();
        String programID = programSpinner.getSelectedItem().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        int yearLevel = getYearLevelInt(yearLevelSpinner.getSelectedItem().toString());

        User updatedUser = new User(userID, email, currentPassword, firstName, lastName, phone, address,
                departmentID, programID, yearLevel, gender, currentUserType, currentProfilePictureUrl);

        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri, () -> saveUserToFirestore(updatedUser));
        } else {
            saveUserToFirestore(updatedUser);
        }
    }

    private void uploadImageToFirebase(Uri imageUri, Runnable onSuccess) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images").child(userID);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    currentProfilePictureUrl = uri.toString();
                    onSuccess.run();
                }))
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void saveUserToFirestore(User user) {
        firestore.collection("Users").document(userID).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int getYearLevelInt(String yearLevelString) {
        switch (yearLevelString) {
            case "1st Year":
                return 1;
            case "2nd Year":
                return 2;
            case "3rd Year":
                return 3;
            case "4th Year":
                return 4;
            case "5th Year":
                return 5;
            case "6th Year":
                return 6;
            default:
                return 0;
        }
    }
}
