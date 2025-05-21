package com.example.counselinlv1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counselinlv1.Adapters.AnalyticsVulnerableStudentsAdapter;
import com.example.counselinlv1.Models.Referral;
import com.example.counselinlv1.R;
import com.example.counselinlv1.Utilities.BaseActivity;
import com.example.counselinlv1.Utilities.StudentScore;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class AnalyticsActivity extends AppCompatActivity {

    private Spinner fromMonthSpinner, fromDaySpinner, fromYearSpinner;
    private Spinner toMonthSpinner, toDaySpinner, toYearSpinner;
    private Spinner filterSpinner;
    private FirebaseFirestore firestore;
    private String currentUserID, departmentID;
    private PieChart pieChart;
    private AnalyticsVulnerableStudentsAdapter studentsAdapter;
    private RecyclerView vulnerableStudentsList;
    private ListenerRegistration referralListener;
    private Button btnViewStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        btnViewStatistics = findViewById(R.id.viewStatisticsButton);
        btnViewStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fromDate = getSelectedDate(fromYearSpinner, fromMonthSpinner, fromDaySpinner);
                String toDate = getSelectedDate(toYearSpinner, toMonthSpinner, toDaySpinner);

                if (fromDate == null || toDate == null) {
                    Toast.makeText(AnalyticsActivity.this, "Please select a valid date range.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Navigate to ReportActivity
                Intent intent = new Intent(AnalyticsActivity.this, ReportActivity.class);
                intent.putExtra("departmentID", departmentID);
                intent.putExtra("fromDate", fromDate);
                intent.putExtra("toDate", toDate);
                startActivity(intent);
            }
        });




        // Initialize Firestore instance
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        currentUserID = intent.getStringExtra("userID");
        departmentID = intent.getStringExtra("departmentID");

        // Initialize spinners
        fromMonthSpinner = findViewById(R.id.fromMonthSpinner);
        fromDaySpinner = findViewById(R.id.fromDaySpinner);
        fromYearSpinner = findViewById(R.id.fromYearSpinner);
        toMonthSpinner = findViewById(R.id.toMonthSpinner);
        toDaySpinner = findViewById(R.id.toDaySpinner);
        toYearSpinner = findViewById(R.id.toYearSpinner);

        // Initialize the PieChart
        pieChart = findViewById(R.id.pieChart);

        filterSpinner = findViewById(R.id.filterSpinner);
        setupFilterSpinner();

        // Populate spinners
        populateMonthSpinner(fromMonthSpinner, toMonthSpinner);
        populateYearSpinner(fromYearSpinner, toYearSpinner);



        vulnerableStudentsList = findViewById(R.id.vulnerableStudentsList);
        setupVulnerableStudentsList();

        // Set default selections
        setDefaultSpinnerSelections();

        listenForReferralUpdates();

        // Set listeners to dynamically update days based on month and year
        setDateChangeListener(fromMonthSpinner, fromYearSpinner, fromDaySpinner);
        setDateChangeListener(toMonthSpinner, toYearSpinner, toDaySpinner);
    }
    // Utility to get the current date in MM/dd/yyyy format
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
    private String getSelectedDateOrDefault(Spinner yearSpinner, Spinner monthSpinner, Spinner daySpinner, String defaultDate) {
        String selectedDate = getSelectedDate(yearSpinner, monthSpinner, daySpinner);
        return selectedDate != null ? selectedDate : defaultDate;
    }


    private void setDefaultSpinnerSelections() {
        // Set default year (current year)
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        setSpinnerSelection(fromYearSpinner, currentYear);
        setSpinnerSelection(toYearSpinner, currentYear);

        // Set default month (January)
        setSpinnerSelection(fromMonthSpinner, "January");
        setSpinnerSelection(toMonthSpinner, "December");

        // Set default day (1st and last of the month)
        setSpinnerSelection(fromDaySpinner, "1");
        setSpinnerSelection(toDaySpinner, "31");
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }



    private void populateMonthSpinner(Spinner... spinners) {
        // Populate the Month Spinners with "January - December"
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Spinner spinner : spinners) {
            spinner.setAdapter(monthAdapter);
        }
    }

    private void populateYearSpinner(Spinner... spinners) {
        // Populate the Year Spinners with example years (2023, 2024, etc.)
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this,
                R.array.years_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Spinner spinner : spinners) {
            spinner.setAdapter(yearAdapter);
        }
    }

    private void setDateChangeListener(Spinner monthSpinner, Spinner yearSpinner, Spinner daySpinner) {
        // Listen for changes in Month and Year spinners to update the Day spinner
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateDaySpinner(monthSpinner, yearSpinner, daySpinner);
                performRegressionAnalysis();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        };
        monthSpinner.setOnItemSelectedListener(listener);
        yearSpinner.setOnItemSelectedListener(listener);
    }

    private void updateDaySpinner(Spinner monthSpinner, Spinner yearSpinner, Spinner daySpinner) {
        // Get selected month and year
        String selectedMonth = monthSpinner.getSelectedItem().toString();
        String selectedYear = yearSpinner.getSelectedItem().toString();

        // Calculate the number of days in the selected month and year
        int daysInMonth = getDaysInMonth(selectedMonth, selectedYear);

        // Populate the Day spinner with the correct number of days
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, generateDaysArray(daysInMonth));
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
    }

    private int getDaysInMonth(String month, String year) {
        // Get the number of days in the selected month and year
        int monthIndex = getMonthIndex(month);
        int yearInt = Integer.parseInt(year);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, yearInt);
        calendar.set(Calendar.MONTH, monthIndex);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private int getMonthIndex(String month) {
        // Convert month name to its corresponding index
        switch (month) {
            case "January":
                return Calendar.JANUARY;
            case "February":
                return Calendar.FEBRUARY;
            case "March":
                return Calendar.MARCH;
            case "April":
                return Calendar.APRIL;
            case "May":
                return Calendar.MAY;
            case "June":
                return Calendar.JUNE;
            case "July":
                return Calendar.JULY;
            case "August":
                return Calendar.AUGUST;
            case "September":
                return Calendar.SEPTEMBER;
            case "October":
                return Calendar.OCTOBER;
            case "November":
                return Calendar.NOVEMBER;
            case "December":
                return Calendar.DECEMBER;
            default:
                return -1;
        }
    }

    private List<String> generateDaysArray(int daysInMonth) {
        // Generate a list of days based on the number of days in the month
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(String.valueOf(i));
        }
        return days;
    }

    private void setupFilterSpinner() {
        // Define the filter options
        String[] filters = {"Gender", "Academic Reason", "Socio/Emotional Reason", "Referral Status"};

        // Create an ArrayAdapter using the filter options
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner
        filterSpinner.setAdapter(filterAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = (String) parent.getItemAtPosition(position);

                // Get selected date range
                String fromDate = getSelectedDate(fromYearSpinner, fromMonthSpinner, fromDaySpinner);
                String toDate = getSelectedDate(toYearSpinner, toMonthSpinner, toDaySpinner);

                // Convert dates to timestamps
                long fromTimestamp = parseDateToTimestamp(fromDate);
                long toTimestamp = parseDateToTimestamp(toDate);

                // Validate timestamps
                if (fromTimestamp == 0 || toTimestamp == 0) {
                    Toast.makeText(AnalyticsActivity.this, "Invalid date range selected.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call loadPieChart with the filter and date range
                loadPieChart(selectedFilter.toLowerCase(), fromTimestamp, toTimestamp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }
    private void setDateChangeListener(Spinner... spinners) {
        AdapterView.OnItemSelectedListener dateChangeListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performRegressionAnalysis(); // Trigger analysis when a date component changes
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        };

        for (Spinner spinner : spinners) {
            spinner.setOnItemSelectedListener(dateChangeListener);
        }
    }



    private void loadPieChart(String filter, long fromTimestamp, long toTimestamp) {
        // Debugging: Log the timestamps
        Log.i("Debug", "From Timestamp: " + fromTimestamp);
        Log.i("Debug", "To Timestamp: " + toTimestamp);

        // Query Firestore with date filter
        firestore.collection("Referrals")
                .whereGreaterThanOrEqualTo("createdAt", fromTimestamp)
                .whereLessThanOrEqualTo("createdAt", toTimestamp)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<PieEntry> entries = new ArrayList<>();
                        List<Referral> referrals = task.getResult().toObjects(Referral.class);

                        // Filter the data based on the selected filter
                        switch (filter.toLowerCase()) {
                            case "gender":
                                loadGenderData(referrals, entries);
                                break;
                            case "academic reason":
                                loadReasonData(referrals, entries, "academicReason");
                                break;
                            case "socio/emotional reason":
                                loadReasonData(referrals, entries, "socialEmotionalReason");
                                break;
                            case "referral status":
                                loadStatusData(referrals, entries);
                                break;
                            default:
                                Toast.makeText(this, "Invalid filter selected.", Toast.LENGTH_SHORT).show();
                                return;
                        }

                        // Update the pie chart
                        PieDataSet dataSet = new PieDataSet(entries, "Distribution");
                        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                        PieData pieData = new PieData(dataSet);

                        pieChart.setData(pieData);
                        pieChart.invalidate(); // Refresh the chart
                    } else {
                        Toast.makeText(this, "Failed to load data for the pie chart.", Toast.LENGTH_SHORT).show();
                        Log.e("Error", "Firestore Query Failed: " + task.getException());
                    }
                });
    }

    private void loadGenderData(List<Referral> referrals, List<PieEntry> entries) {
        Map<String, Integer> genderCount = new TreeMap<>();
        int[] completedQueries = {0}; // To track completed Firestore queries

        for (Referral referral : referrals) {
            String studentID = referral.getStudentID();

            // Query Firestore to fetch gender based on studentID
            firestore.collection("Users")
                    .document(studentID)
                    .get()
                    .addOnCompleteListener(task -> {
                        completedQueries[0]++; // Increment completed queries count

                        if (task.isSuccessful() && task.getResult() != null) {
                            String gender = task.getResult().getString("gender");
                            if (gender != null) {
                                genderCount.put(gender, genderCount.getOrDefault(gender, 0) + 1);
                            }
                        }

                        // When all queries are completed, populate the pie chart
                        if (completedQueries[0] == referrals.size()) {
                            entries.clear();
                            for (Map.Entry<String, Integer> entry : genderCount.entrySet()) {
                                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                            }
                            updatePieChart(entries, "Gender Distribution");
                        }
                    });
        }
    }

    // Utility function to update the PieChart
    private void updatePieChart(List<PieEntry> entries, String description) {
        PieDataSet dataSet = new PieDataSet(entries, description);
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(18f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.setCenterText(description);
        pieChart.setCenterTextSize(18f);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate(); // Refresh the chart
    }

    private void loadReasonData(List<Referral> referrals, List<PieEntry> entries, String reasonType) {
        Map<String, Integer> reasonCount = new TreeMap<>();

        for (Referral referral : referrals) {
            String reason;

            // Check if we're working with academic or social/emotional reasons
            if ("academicReason".equals(reasonType)) {
                reason = referral.getAcademicReason().toLowerCase();

                // Apply rules for academic reasons
                if ("na".equals(reason)) {
                    continue; // Skip "NA"
                }
                if (!"attendance".equals(reason) && !"performance".equals(reason)) {
                    reason = "others"; // Group anything else under "Others"
                }
            } else {
                reason = referral.getSocialEmotionalReason().toLowerCase();

                // Apply rules for social/emotional reasons
                if ("na".equals(reason)) {
                    continue; // Skip "NA"
                }
                if (!"personal".equals(reason) && !"friends".equals(reason)
                        && !"love life".equals(reason) && !"family".equals(reason)) {
                    reason = "others"; // Group anything else under "Others"
                }
            }

            // Update the reason count
            reasonCount.put(reason, reasonCount.getOrDefault(reason, 0) + 1);
        }

        // Populate entries for the pie chart
        entries.clear(); // Clear previous entries
        for (Map.Entry<String, Integer> entry : reasonCount.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        // Update the pie chart
        updatePieChart(entries, reasonType.equals("academicReason") ? "Academic Reasons" : "Social/Emotional Reasons");
    }

    private void loadStatusData(List<Referral> referrals, List<PieEntry> entries) {
        Map<String, Integer> statusCount = new TreeMap<>();

        // Aggregate status data
        for (Referral referral : referrals) {
            String status = referral.getStatus();
            if (status != null && !status.trim().isEmpty()) { // Avoid null or empty statuses
                statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
            }
        }

        // Populate entries for the pie chart
        entries.clear(); // Clear previous entries
        for (Map.Entry<String, Integer> entry : statusCount.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        // Update the pie chart
        updatePieChart(entries, "Referral Status Distribution");
    }

    // Helper to group referrals by date
    private Map<String, Integer> groupByDate(List<Referral> referrals) {
        Map<String, Integer> trend = new TreeMap<>();

        for (Referral referral : referrals) {
            String date = formatDate(referral.getCreatedAt());
            trend.put(date, trend.getOrDefault(date, 0) + 1);
        }

        return trend;
    }

    private String getSelectedDate(Spinner yearSpinner, Spinner monthSpinner, Spinner daySpinner) {
        try {
            String year = yearSpinner.getSelectedItem().toString();
            String month = String.valueOf(monthSpinner.getSelectedItemPosition() + 1); // Convert to 1-indexed month
            String day = daySpinner.getSelectedItem().toString();

            // Format the date as MM/dd/yyyy
            return String.format(Locale.getDefault(), "%02d/%02d/%s", Integer.parseInt(month), Integer.parseInt(day), year);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if there's an error
        }
    }


    private Long parseDateToTimestamp(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            if (date != null) {
                return date.getTime(); // Return milliseconds
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null; // Return null if parsing fails
    }

    // Helper to format timestamp into "MM/dd/yyyy"
    private String formatDate(long timestamp) {
        return android.text.format.DateFormat.format("MM/dd/yyyy", new java.util.Date(timestamp)).toString();
    }

    private void performRegressionAnalysis() {
        // Retrieve date range from the spinners
        String fromDate = getSelectedDate(fromYearSpinner, fromMonthSpinner, fromDaySpinner);
        String toDate = getSelectedDate(toYearSpinner, toMonthSpinner, toDaySpinner);

        if (fromDate == null || toDate == null) {
            Toast.makeText(this, "Please select a valid date range.", Toast.LENGTH_SHORT).show();
            return;
        }

        long fromTimestamp = parseDateToTimestamp(fromDate);
        long toTimestamp = parseDateToTimestamp(toDate);

        // Fetch referrals within the selected date range
        firestore.collection("Referrals")
                .whereGreaterThanOrEqualTo("createdAt", fromTimestamp)
                .whereLessThanOrEqualTo("createdAt", toTimestamp)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Referral> referrals = task.getResult().toObjects(Referral.class);
                        Map<String, Double> vulnerabilityScores = calculateVulnerabilityScores(referrals);

                        if (referrals.isEmpty()) {
                            // If no referrals match, update RecyclerView with an empty list
                            updateVulnerableStudentsList(new ArrayList<>());
                            return; // Stop further execution
                        }

                        // Sort the students by their vulnerability score
                        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(vulnerabilityScores.entrySet());
                        sortedScores.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

                        for (Map.Entry<String, Double> entry : sortedScores) {
                            Log.d("DEBUG", "Student ID: " + entry.getKey() + ", Score: " + entry.getValue());
                        }


                        // Fetch the top 10 most vulnerable students
                        List<StudentScore> topStudents = new ArrayList<>();
                        for (int i = 0; i < Math.min(sortedScores.size(), 10); i++) {
                            String studentID = sortedScores.get(i).getKey();
                            double score = sortedScores.get(i).getValue();

                            firestore.collection("Users")
                                    .document(studentID)
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                                            String studentName = userTask.getResult().getString("firstName") + " " + userTask.getResult().getString("lastName");
                                            if (studentName == null || studentName.trim().isEmpty()) {
                                                studentName = "Unknown Student"; // Fallback if no name is found
                                            }
                                            Log.d("DEBUG", "Student Name: " + studentName + ", Score: " + score);
                                            topStudents.add(new StudentScore(studentName, score));

                                            // Update RecyclerView when all data is fetched
                                            if (topStudents.size() == Math.min(sortedScores.size(), 10)) {
                                                updateVulnerableStudentsList(topStudents);
                                            }
                                        } else {
                                            Log.e("ERROR", "Failed to fetch student name for ID: " + studentID);
                                        }
                                    });
                        }

                    } else {
                        Toast.makeText(this, "Failed to fetch referral data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Function to calculate vulnerability scores using regression analysis
    private Map<String, Double> calculateVulnerabilityScores(List<Referral> referrals) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Integer> referralCount = new HashMap<>();
        Map<String, Integer> academicCount = new HashMap<>();
        Map<String, Integer> socioEmotionalCount = new HashMap<>();
        Map<String, Integer> pendingCount = new HashMap<>();

        for (Referral referral : referrals) {
            String studentID = referral.getStudentID();

            // Count total referrals for each student
            referralCount.put(studentID, referralCount.getOrDefault(studentID, 0) + 1);

            // Count academic-related referrals
            if (!"NA".equals(referral.getAcademicReason())) {
                academicCount.put(studentID, academicCount.getOrDefault(studentID, 0) + 1);
            }

            // Count socio-emotional-related referrals
            if (!"NA".equals(referral.getSocialEmotionalReason())) {
                socioEmotionalCount.put(studentID, socioEmotionalCount.getOrDefault(studentID, 0) + 1);
            }

            // Count pending referrals
            if ("Pending".equalsIgnoreCase(referral.getStatus())) {
                pendingCount.put(studentID, pendingCount.getOrDefault(studentID, 0) + 1);
            }
        }

        // Calculate scores for each student using regression weights
        for (String studentID : referralCount.keySet()) {
            double academicFactor = 0.4 * academicCount.getOrDefault(studentID, 0);
            double socioEmotionalFactor = 0.25 * socioEmotionalCount.getOrDefault(studentID, 0);
            double referralFactor = 0.2 * referralCount.getOrDefault(studentID, 0);
            double pendingFactor = 0.15 * pendingCount.getOrDefault(studentID, 0);

            double score = academicFactor + socioEmotionalFactor + referralFactor + pendingFactor;
            scores.put(studentID, score);
        }

        return scores;
    }

    private void updateVulnerableStudentsList(List<StudentScore> topStudents) {
        studentsAdapter.updateData(topStudents); // Refresh the list in the adapter
        studentsAdapter.notifyDataSetChanged(); // Notify the adapter about data changes
    }


    private void setupVulnerableStudentsList() {
        studentsAdapter = new AnalyticsVulnerableStudentsAdapter(new ArrayList<>());
        vulnerableStudentsList.setLayoutManager(new LinearLayoutManager(this));
        vulnerableStudentsList.setAdapter(studentsAdapter);
    }

    private void listenForReferralUpdates() {
        // Replace "departmentID" with the counselor's department ID
        String counselorDepartmentID = departmentID;

        referralListener = firestore.collection("Referrals")
                .whereEqualTo("departmentID", counselorDepartmentID)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to listen for updates.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<Referral> updatedReferrals = querySnapshot.toObjects(Referral.class);

                        // Recalculate the regression scores using calculateVulnerabilityScores
                        Map<String, Double> vulnerabilityScores = calculateVulnerabilityScores(updatedReferrals);

                        // Sort the students by their vulnerability score
                        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(vulnerabilityScores.entrySet());
                        sortedScores.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

                        // Fetch the top 10 most vulnerable students
                        List<StudentScore> topStudents = new ArrayList<>();
                        for (int i = 0; i < Math.min(sortedScores.size(), 10); i++) {
                            String studentID = sortedScores.get(i).getKey();
                            double score = sortedScores.get(i).getValue();

                            firestore.collection("Users")
                                    .document(studentID)
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                                            String studentName = userTask.getResult().getString("studentName");
                                            topStudents.add(new StudentScore(studentName, score));

                                            // Update the RecyclerView when all data is fetched
                                            if (topStudents.size() == Math.min(sortedScores.size(), 10)) {
                                                updateVulnerableStudentsList(topStudents);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (referralListener != null) {
            referralListener.remove();
        }
    }

}
