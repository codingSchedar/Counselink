package com.example.counselinlv1;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.counselinlv1.Models.Referral;
import com.example.counselinlv1.Utilities.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private TextView statusPending, statusProcessing, statusCounseled, statusFollowUp, statusDone, statusTotal;
    private TextView genderMale, genderFemale, genderTotal;
    private TextView academicAttendance, academicPerformance, academicOthers, academicTotal;
    private TextView socioPersonal, socioFriends, socioLoveLife, socioFamily, socioOthers, socioTotal;
    private ImageView btnDownload;

    private String departmentID;
    private long fromTimestamp, toTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get extras from Intent
        Intent intent = getIntent();
        departmentID = intent.getStringExtra("departmentID");
        String fromDate = intent.getStringExtra("fromDate");
        String toDate = intent.getStringExtra("toDate");


        // Initialize TextViews
        initializeViews();

        if (departmentID == null || fromDate == null || toDate == null) {
            Toast.makeText(this, "Invalid data passed to the report screen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fromTimestamp = parseDateToTimestamp(fromDate);
        toTimestamp = parseDateToTimestamp(toDate);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // No permissions required, save using MediaStore
                    generateReportWordFileUsingScopedStorage();
                } else {
                    // Generate and save the report for older versions
                    generateReportWordFile();
                }
            }
        });



        // Fetch and update data
        fetchAndUpdateReport();
    }

    private void initializeViews() {
        // Status
        statusPending = findViewById(R.id.status_pending);
        statusProcessing = findViewById(R.id.status_processing);
        statusCounseled = findViewById(R.id.status_counseled);
        statusFollowUp = findViewById(R.id.status_follow_up);
        statusDone = findViewById(R.id.status_done);
        statusTotal = findViewById(R.id.status_total);

        // Gender
        genderMale = findViewById(R.id.gender_male);
        genderFemale = findViewById(R.id.gender_female);
        genderTotal = findViewById(R.id.gender_total);

        // Academic Reasons
        academicAttendance = findViewById(R.id.academic_attendance);
        academicPerformance = findViewById(R.id.academic_performance);
        academicOthers = findViewById(R.id.academic_others);
        academicTotal = findViewById(R.id.academic_total);

        // Socio/Emotional Reasons
        socioPersonal = findViewById(R.id.socio_personal);
        socioFriends = findViewById(R.id.socio_friends);
        socioLoveLife = findViewById(R.id.socio_love_life);
        socioFamily = findViewById(R.id.socio_family);
        socioTotal = findViewById(R.id.socio_total);
        socioOthers = findViewById(R.id.socio_others);

        btnDownload = findViewById(R.id.download_icon);
    }

    private void fetchAndUpdateReport() {
        // Step 1: Query the Users table to get studentIDs for the given departmentID
        firestore.collection("Users")
                .whereEqualTo("departmentID", departmentID)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && userTask.getResult() != null) {
                        List<String> studentIDs = new ArrayList<>();
                        userTask.getResult().forEach(document -> studentIDs.add(document.getId()));

                        if (studentIDs.isEmpty()) {
                            // If no students belong to this department, clear all sections
                            clearAllSections();
                            Toast.makeText(this, "No students found for the selected department.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Step 2: Query the Referrals table using the studentIDs and date range
                        firestore.collection("Referrals")
                                .whereIn("studentID", studentIDs)
                                .whereGreaterThanOrEqualTo("createdAt", fromTimestamp)
                                .whereLessThanOrEqualTo("createdAt", toTimestamp)
                                .get()
                                .addOnCompleteListener(referralTask -> {
                                    if (referralTask.isSuccessful() && referralTask.getResult() != null) {
                                        List<Referral> referrals = referralTask.getResult().toObjects(Referral.class);

                                        if (referrals.isEmpty()) {
                                            clearAllSections();
                                        } else {
                                            updateStatusSection(referrals);
                                            updateGenderSection(referrals);
                                            updateAcademicReasonSection(referrals);
                                            updateSocioEmotionalReasonSection(referrals);
                                        }
                                    } else {
                                        Toast.makeText(this, "Failed to load referral data.", Toast.LENGTH_SHORT).show();
                                        Log.e("ReportActivity", "Error fetching referral data: ", referralTask.getException());
                                        clearAllSections();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                        Log.e("ReportActivity", "Error fetching user data: ", userTask.getException());
                        clearAllSections();
                    }
                });
    }

    private void updateStatusSection(List<Referral> referrals) {
        int pending = 0, processing = 0, counseled = 0, followUp = 0, done = 0, total = 0;

        for (Referral referral : referrals) {
            switch (referral.getStatus().toLowerCase()) {
                case "pending":
                    pending++;
                    break;
                case "processing":
                    processing++;
                    break;
                case "counselled":
                    counseled++;
                    break;
                case "follow-up":
                    followUp++;
                    break;
                case "done":
                    done++;
                    break;

            }
        }

        statusPending.setText("Pending - " + pending);
        statusProcessing.setText("Processing - " + processing);
        statusCounseled.setText("Counseled - " + counseled);
        statusFollowUp.setText("Follow-up - " + followUp);
        statusDone.setText("Done - " + done);
        statusTotal.setText("Total - " + (pending + processing + counseled + followUp + done));
    }

    private void updateGenderSection(List<Referral> referrals) {
        AtomicInteger male = new AtomicInteger(0);
        AtomicInteger female = new AtomicInteger(0);

        for (Referral referral : referrals) {
            firestore.collection("Users")
                    .document(referral.getStudentID())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String gender = task.getResult().getString("gender");
                            if ("male".equalsIgnoreCase(gender)) {
                                male.incrementAndGet();
                            } else if ("female".equalsIgnoreCase(gender)) {
                                female.incrementAndGet();
                            }

                            // Update the UI with the latest counts
                            genderMale.setText("Total Male - " + male.get());
                            genderFemale.setText("Total Female - " + female.get());
                            genderTotal.setText("Total - " + (male.get() + female.get()));
                        } else {
                            Log.e("ReportActivity", "Error fetching gender for student ID: " + referral.getStudentID());
                        }
                    });
        }
    }

    private void updateAcademicReasonSection(List<Referral> referrals) {
        int attendance = 0, performance = 0, others = 0;

        for (Referral referral : referrals) {
            String reason = referral.getAcademicReason().toLowerCase();
            if ("attendance".equalsIgnoreCase(reason)) attendance++;
            else if ("performance".equalsIgnoreCase(reason)) performance++;
            else if ("na".equalsIgnoreCase(reason)) ;
            else others++;
        }

        academicAttendance.setText("Attendance - " + attendance);
        academicPerformance.setText("Performance - " + performance);
        academicOthers.setText("Others - " + others);
        academicTotal.setText("Total - " + (attendance + performance + others));
    }

    private void updateSocioEmotionalReasonSection(List<Referral> referrals) {
        int personal = 0, friends = 0, loveLife = 0, family = 0, others = 0;

        for (Referral referral : referrals) {
            String reason = referral.getSocialEmotionalReason().toLowerCase();
            if ("personal".equalsIgnoreCase(reason)) personal++;
            else if ("friends".equalsIgnoreCase(reason)) friends++;
            else if ("love life".equalsIgnoreCase(reason)) loveLife++;
            else if ("family".equalsIgnoreCase(reason)) family++;
            else if ("na".equalsIgnoreCase(reason)) ;
            else others++;
        }

        socioPersonal.setText("Personal - " + personal);
        socioFriends.setText("Friends - " + friends);
        socioLoveLife.setText("Love Life - " + loveLife);
        socioFamily.setText("Family - " + family);
        socioOthers.setText("Others - " + others);
        socioTotal.setText("Total - " + (personal + friends + loveLife + family + others));
    }

    private void clearAllSections() {
        clearStatusSection();
        clearGenderSection();
        clearAcademicReasonSection();
        clearSocioEmotionalReasonSection();
    }

    private void clearStatusSection() {
        statusPending.setText("Pending - 0");
        statusProcessing.setText("Processing - 0");
        statusCounseled.setText("Counseled - 0");
        statusFollowUp.setText("Follow-up - 0");
        statusDone.setText("Done - 0");
    }

    private void clearGenderSection() {
        genderMale.setText("Total Male - 0");
        genderFemale.setText("Total Female - 0");
        genderTotal.setText("Total - 0");
    }

    private void clearAcademicReasonSection() {
        academicAttendance.setText("Attendance - 0");
        academicPerformance.setText("Performance - 0");
        academicTotal.setText("Total - 0");
    }

    private void clearSocioEmotionalReasonSection() {
        socioPersonal.setText("Personal - 0");
        socioFriends.setText("Friends - 0");
        socioLoveLife.setText("Love life - 0");
        socioFamily.setText("Family - 0");
        socioTotal.setText("Total - 0");
    }

    private Long parseDateToTimestamp(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            return (date != null) ? date.getTime() : null;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void generateReportWordFile() {
        try {
            // Create a new document
            XWPFDocument document = generateWordDocument();

            // Save the document
            String fileName = "Counseling_Report_" + System.currentTimeMillis() + ".docx";
            File file = new File(getExternalFilesDir(null), fileName);
            FileOutputStream out = new FileOutputStream(file);
            document.write(out);
            out.close();
            document.close();

            // Notify the user of success
            Toast.makeText(this, "Report saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create report.", Toast.LENGTH_SHORT).show();
        }
    }

    private XWPFDocument generateWordDocument() {
        try {
            // Create a new document
            XWPFDocument document = new XWPFDocument();

            // Add a title to the document
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Counseling Report");
            titleRun.setBold(true);
            titleRun.setFontSize(20);

            // Add a section for status
            addSectionToWordDoc(document, "Status",
                    statusPending.getText().toString(),
                    statusProcessing.getText().toString(),
                    statusCounseled.getText().toString(),
                    statusFollowUp.getText().toString(),
                    statusDone.getText().toString(),
                    statusTotal.getText().toString());

            // Add a section for gender
            addSectionToWordDoc(document, "Gender",
                    genderMale.getText().toString(),
                    genderFemale.getText().toString(),
                    genderTotal.getText().toString());

            // Add a section for academic reasons
            addSectionToWordDoc(document, "Academic Reason - Referral",
                    academicAttendance.getText().toString(),
                    academicPerformance.getText().toString(),
                    academicOthers.getText().toString(),
                    academicTotal.getText().toString());

            // Add a section for social/emotional reasons
            addSectionToWordDoc(document, "Social/Emotional Reason - Referral",
                    socioPersonal.getText().toString(),
                    socioFriends.getText().toString(),
                    socioLoveLife.getText().toString(),
                    socioFamily.getText().toString(),
                    socioOthers.getText().toString(),
                    socioTotal.getText().toString());

            return document;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating document content.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void addSectionToWordDoc(XWPFDocument document, String title, String... lines) {
        // Add a section title
        XWPFParagraph sectionTitle = document.createParagraph();
        sectionTitle.setSpacingBefore(200);
        XWPFRun sectionTitleRun = sectionTitle.createRun();
        sectionTitleRun.setText(title);
        sectionTitleRun.setBold(true);
        sectionTitleRun.setFontSize(16);

        // Add content lines
        for (String line : lines) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(line);
            run.setFontSize(12);
        }
    }

    private void generateReportWordFileUsingScopedStorage() {
        try {
            // Define the file name
            String fileName = "Counseling_Report_" + System.currentTimeMillis() + ".docx";

            // Create the content values to store the file
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            // Insert the file into MediaStore
            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);

            if (uri != null) {
                // Open an output stream to write the file
                OutputStream outputStream = getContentResolver().openOutputStream(uri);

                if (outputStream != null) {
                    // Generate the document content
                    XWPFDocument document = generateWordDocument();

                    // Write the document to the output stream
                    document.write(outputStream);
                    outputStream.close();

                    Toast.makeText(this, "Report saved to Downloads", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save the report", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create the report file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating the report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
