package com.alya.aplikasilansia.ui.reminder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.ui.newreminder.IconReminderFragment;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditReminderActivity extends AppCompatActivity implements IconReminderFragment.OnIconSelectedListener {
    private Button backBtn, saveBtn, cancelBtn, editIconBtn;
    private TextView timePickerTv;
    private EditText titleEt, descEt;
    private ImageView iconImg;
    private Spinner daySpinner;
    private String reminderId, daySelected;
    private int selectedIcon;
    ReminderViewModel reminderViewModel;
    
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminder);

        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);

        iconImg = findViewById(R.id.img_ic_reminder_edit);
        titleEt = findViewById(R.id.et_remind_title);
        daySpinner = findViewById(R.id.edit_day_reminder);
        timePickerTv = findViewById(R.id.tv_hour_reminder_edit);
        descEt = findViewById(R.id.et_desc_reminder);
        editIconBtn = findViewById(R.id.btn_ic_remind_edit);
        saveBtn = findViewById(R.id.btn_save_edit_reminder);
        cancelBtn = findViewById(R.id.btn_cancel_edit_reminder);
        backBtn = findViewById(R.id.btn_back_editreminder);

        dialogIconReminder(editIconBtn);
        setTimePicker(timePickerTv);
        getReminderData();

        cancelBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EditReminderActivity.this, ReminderActivity.class);
            startActivity(intent);
            finish();
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EditReminderActivity.this, ReminderActivity.class);
            startActivity(intent);
            finish();
        });

        saveBtn.setOnClickListener(v -> {
            saveEditedData();
            Intent intent = new Intent(EditReminderActivity.this, ReminderActivity.class);
            startActivity(intent);
            finish();
        });

        reminderViewModel.errorLiveData.observe(this, errorMessage -> {
            if (errorMessage != null) {
                // Show error message (e.g., using a Toast)
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getReminderData() {
        Intent intent = getIntent();
        reminderId = intent.getStringExtra("REMINDER_ID");
        titleEt.setText(intent.getStringExtra("REMINDER_TITLE"));
        daySelected = intent.getStringExtra("REMINDER_DAY");
        setDaySpinner(daySelected);
        timePickerTv.setText(intent.getStringExtra("REMINDER_TIME"));
        descEt.setText(intent.getStringExtra("REMINDER_DESC"));
        selectedIcon = intent.getIntExtra("REMINDER_ICON", 1);
        iconImg.setImageResource(selectedIcon);
    }

    private void saveEditedData() {
        String newTitle = titleEt.getText().toString().trim();
        String newDay = daySpinner.getSelectedItem().toString();
        String newTime = timePickerTv.getText().toString().trim();
        String desc = descEt.getText().toString().trim();
        String timestamp = calculateTimestamp(newDay, newTime);

        if (reminderId != null){
            reminderViewModel.editReminder(reminderId, newTitle, newDay, newTime, desc, timestamp, selectedIcon, () -> {
                // This code runs after the reminder is successfully edited
//                Toast.makeText(this, "Reminder updated successfully!", Toast.LENGTH_SHORT).show();
                // Optionally, navigate back or refresh UI
                dataSavedDialog();
                finish(); // Close the activity if you are in an edit activity
                // Or refresh the list in your RecyclerView, etc.
            });

        }
    }

    private void dataSavedDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.data_saved_dialog, null);

        ImageView toastIcon = layout.findViewById(R.id.img_verif_sent);
        TextView toastText = layout.findViewById(R.id.text_verif_sent);

        String text = "Pengingat Berhasil Diperbarui";

        toastIcon.setImageResource(R.drawable.ic_checkmark);
        toastText.setText(text);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void dialogIconReminder(Button editIconBtn) {
        editIconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IconReminderFragment iconReminderFragment = new IconReminderFragment();
                iconReminderFragment.show(getSupportFragmentManager(), "IconReminderDialog");
//                Toast.makeText(this, "Button Clicked!", Toast.LENGTH_SHORT).show();
                iconReminderFragment.setOnIconSelectedListener(EditReminderActivity.this); // Set listener
            }
        });
    }

    @Override
    public void onIconSelected(int iconResId) {
        // Update ImageView with selected icon
        iconImg.setImageResource(iconResId);
        selectedIcon = iconResId; // Save selected icon resource ID to use later
    }

    private void setDaySpinner(String selectedDay) {
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.day_array)
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        daySpinner.setAdapter(spinnerAdapter);

        if (selectedDay != null) {
            int position = spinnerAdapter.getPosition(selectedDay);
            daySpinner.setSelection(position);
        }
    }

    private void setTimePicker(TextView timePickerTv) {
        timePickerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
    }

    private void showTimePicker() {
        MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .build();

        materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = materialTimePicker.getHour();
                int minute = materialTimePicker.getMinute();
                String selectedTime = String.format("%02d:%02d", hour, minute);
                timePickerTv.setText(selectedTime);
            }
        });

        materialTimePicker.show(getSupportFragmentManager(), "timePicker");
    }

    private String calculateTimestamp(String selectedDay, String selectedTime) {
        Calendar now = Calendar.getInstance();

        // Parse selected time
        String[] timeParts = selectedTime.split(":");
        if (timeParts.length != 2) {
            // Handle invalid time format
            return "";
        }

        int hour = 0;
        int minute = 0;

        try {
            hour = Integer.parseInt(timeParts[0]);
            minute = Integer.parseInt(timeParts[1]);
        } catch (NumberFormatException e) {
            // Handle parsing error
            return "";
        }

        // Calculate the day difference
        int dayOfWeekNow = now.get(Calendar.DAY_OF_WEEK);
        int targetDayOfWeek = getDayOfWeek(selectedDay);
        if (targetDayOfWeek == -1) {
            // Handle invalid day
            return "";
        }
        int dayDifference = (targetDayOfWeek - dayOfWeekNow + 7) % 7;

        // Set the target date and time
        Calendar targetDate = (Calendar) now.clone();
        targetDate.add(Calendar.DAY_OF_MONTH, dayDifference);
        targetDate.set(Calendar.HOUR_OF_DAY, hour);
        targetDate.set(Calendar.MINUTE, minute);
        targetDate.set(Calendar.SECOND, 0);
        targetDate.set(Calendar.MILLISECOND, 0);

        // Return the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(targetDate.getTime());
    }

    private int getDayOfWeek(String day) {
        switch (day) {
            case "Senin":
                return Calendar.MONDAY;
            case "Selasa":
                return Calendar.TUESDAY;
            case "Rabu":
                return Calendar.WEDNESDAY;
            case "Kamis":
                return Calendar.THURSDAY;
            case "Jumat":
                return Calendar.FRIDAY;
            case "Sabtu":
                return Calendar.SATURDAY;
            case "Minggu":
                return Calendar.SUNDAY;
            default:
                return -1; // Handle invalid input or edge cases
        }
    }
}