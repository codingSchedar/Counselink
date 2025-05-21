package com.example.counselinlv1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.counselinlv1.Models.User;
import com.example.counselinlv1.Utilities.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class PersonalInformationActivity extends AppCompatActivity {

    private Spinner departmentSpinner, programSpinner, yearLevelSpinner, genderSpinner;
    private String userID, email, password;
    private Button btnContinue;
    private ProgressDialog progressDialog;
    private EditText etFirstName, etLastName, etPhone, etAddress;

    private FirebaseFirestore firestore;

    // Dictionary to map Year Level string to integer
    private static final HashMap<String, Integer> yearLevelMap = new HashMap<String, Integer>() {{
        put("1st Year", 1);
        put("2nd Year", 2);
        put("3rd Year", 3);
        put("4th Year", 4);
        put("5th Year", 5);
        put("6th Year", 6);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        firestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);

        userID = getIntent().getStringExtra("userID");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        etFirstName = findViewById(R.id.first_name_input);
        etLastName = findViewById(R.id.last_name_input);
        etPhone = findViewById(R.id.phone_input);
        etAddress = findViewById(R.id.address_input);

        departmentSpinner = findViewById(R.id.department_spinner);
        programSpinner = findViewById(R.id.program_spinner);
        yearLevelSpinner = findViewById(R.id.year_level_spinner);
        genderSpinner = findViewById(R.id.gender_spinner);
        btnContinue = findViewById(R.id.continue_button);

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
                updateProgramSpinner(selectedDepartment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        btnContinue.setOnClickListener(view -> checkInfo());
    }

    private void updateProgramSpinner(String department) {
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

        // Populate the Program spinner based on selected department
        ArrayAdapter<CharSequence> programAdapter = ArrayAdapter.createFromResource(
                this, arrayId, android.R.layout.simple_spinner_item);
        programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        programSpinner.setAdapter(programAdapter);
    }

    private void registerUser(String userID, String email, String password, String firstName,
                              String lastName, String phone, String address, String departmentID,
                              String programID, Integer yearLevel, String gender, String type) {

        // Create a new User object
        User user = new User(userID, email, password, firstName, lastName, phone, address,
                departmentID, programID, yearLevel, gender, "student", null);

        // Store the user data in Firestore
        firestore.collection("Users").document(userID)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PersonalInformationActivity.this, "Account Registered", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        Intent intent = new Intent(PersonalInformationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(PersonalInformationActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkInfo() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String departmentID = departmentSpinner.getSelectedItem().toString().trim();
        String programID = programSpinner.getSelectedItem().toString().trim();
        String gender = String.valueOf(genderSpinner.getSelectedItem().toString());

        // Get year level from spinner and map it to an integer
        String yearLevelStr = yearLevelSpinner.getSelectedItem().toString().trim();
        Integer yearLevel = yearLevelMap.get(yearLevelStr);

        progressDialog.setTitle("Registering");
        progressDialog.setMessage("Verifying information");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        registerUser(userID, email, password, firstName,
                lastName, phone, address, departmentID,
                programID, yearLevel, gender, "student");
    }
}
