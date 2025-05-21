package com.example.counselinlv1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.counselinlv1.Utilities.BaseActivity;

import java.util.Calendar;

public class SetNewAppointmentActivity extends AppCompatActivity {

    // Declare the Spinners for Date and Time
    Spinner monthSpinner, yearSpinner, daySpinner, hourSpinner, minuteSpinner, ampmSpinner;
    Button setDateTimeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_appointment);

        // Initialize the Spinners
        monthSpinner = findViewById(R.id.month_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        daySpinner = findViewById(R.id.day_spinner);
        hourSpinner = findViewById(R.id.hour_spinner);
        minuteSpinner = findViewById(R.id.minute_spinner);
        ampmSpinner = findViewById(R.id.ampm_spinner);
        setDateTimeButton = findViewById(R.id.set_date_time_button);

        // Populate the month and year spinners
        populateMonthSpinner();
        populateYearSpinner();

        // Populate the time spinners
        populateTimeSpinners();

        // Set listeners for the spinners
        setMonthYearListener();

        // Set the listener for the Set Date & Time button
        setDateTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Set Date & Time" button click
                String selectedDate = getSelectedDate();
                String selectedTime = getSelectedTime();
                Toast.makeText(SetNewAppointmentActivity.this, "Appointment set for: " + selectedDate + " at " + selectedTime, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateMonthSpinner() {
        // Populate the Month Spinner (January - December)
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
    }

    private void populateYearSpinner() {
        // Populate the Year Spinner (example: 2023, 2024, etc.)
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(this,
                R.array.years_array, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
    }

    private void populateTimeSpinners() {
        // Populate the Hour Spinner (01-12)
        ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(this,
                R.array.hours_array, android.R.layout.simple_spinner_item);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);

        // Populate the Minute Spinner (00-59)
        ArrayAdapter<CharSequence> minuteAdapter = ArrayAdapter.createFromResource(this,
                R.array.minutes_array, android.R.layout.simple_spinner_item);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);

        // Populate the AM/PM Spinner
        ArrayAdapter<CharSequence> ampmAdapter = ArrayAdapter.createFromResource(this,
                R.array.ampm_array, android.R.layout.simple_spinner_item);
        ampmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ampmSpinner.setAdapter(ampmAdapter);
    }

    private void setMonthYearListener() {
        // Listen for changes in the Month and Year spinners
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateDaySpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateDaySpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    private void updateDaySpinner() {
        // Get the selected month and year
        String selectedMonth = monthSpinner.getSelectedItem().toString();
        String selectedYear = yearSpinner.getSelectedItem().toString();

        // Get the number of days in the selected month and year
        int daysInMonth = getDaysInMonth(selectedMonth, selectedYear);

        // Populate the Day spinner with the correct number of days
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getDaysArray(daysInMonth));
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
    }

    private String[] getDaysArray(int daysInMonth) {
        // Create an array of days based on the number of days in the selected month
        String[] days = new String[daysInMonth];
        for (int i = 0; i < daysInMonth; i++) {
            days[i] = String.valueOf(i + 1);
        }
        return days;
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
        // Convert the month name to its index
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

    private String getSelectedDate() {
        // Get the selected date
        String selectedMonth = monthSpinner.getSelectedItem().toString();
        String selectedDay = daySpinner.getSelectedItem().toString();
        String selectedYear = yearSpinner.getSelectedItem().toString();
        return selectedMonth + " " + selectedDay + ", " + selectedYear;
    }

    private String getSelectedTime() {
        // Get the selected time
        String selectedHour = hourSpinner.getSelectedItem().toString();
        String selectedMinute = minuteSpinner.getSelectedItem().toString();
        String selectedAmPm = ampmSpinner.getSelectedItem().toString();
        return selectedHour + ":" + selectedMinute + " " + selectedAmPm;
    }
}
