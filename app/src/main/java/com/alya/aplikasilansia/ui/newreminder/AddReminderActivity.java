package com.alya.aplikasilansia.ui.newreminder;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.messaging.ReminderScheduler;
import com.alya.aplikasilansia.ui.reminder.CustomSpinnerAdapter;
import com.alya.aplikasilansia.ui.reminder.ReminderActivity;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddReminderActivity extends AppCompatActivity implements View.OnClickListener, IconReminderFragment.OnIconSelectedListener {
    private AddReminderViewModel addReminderViewModel;
    private int selectedIconResourceId;
    private TextView tvHourReminder;
    private Button btnIconReminder, btnCreateReminder, btnCancel, btnBackFromAddReminder;
    private ImageView imgIconReminder;
    private EditText inputTitleReminder;
    private EditText inputDescReminder;
    private Spinner dayReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        addReminderViewModel = new ViewModelProvider(this).get(AddReminderViewModel.class);

        imgIconReminder = findViewById(R.id.img_edit_ic_reminder);
        btnIconReminder = findViewById(R.id.btn_edit_ic_reminder);
        btnCancel = findViewById(R.id.btn_cancel_reminder);
        btnBackFromAddReminder = findViewById(R.id.btn_back_addreminder);
        btnBackFromAddReminder.setOnClickListener(this);
        btnCreateReminder = findViewById(R.id.btn_create_reminder);
        btnCreateReminder.setOnClickListener(this);
        tvHourReminder = findViewById(R.id.tv_hour_reminder);
        inputTitleReminder = findViewById(R.id.input_judul_reminder);
        inputDescReminder = findViewById(R.id.input_desc_reminder);
        dayReminder = findViewById(R.id.spinner_day_reminder);

        dialogIconReminder(btnIconReminder);
        setTimePicker(tvHourReminder);

        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(AddReminderActivity.this, ReminderActivity.class);
            startActivity(intent);
            finish();
        });

        CustomSpinnerAdapter dayReminderAdapter = new CustomSpinnerAdapter(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.day_array)
        );
        dayReminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayReminder.setAdapter(dayReminderAdapter);

        addReminderViewModel.reminderLiveData.observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    dataSavedDialog();
                    Log.d(TAG, "createReminder:success");
                    Intent intent = new Intent(AddReminderActivity.this, ReminderActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        addReminderViewModel.errorLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null) {
                    Log.w(TAG, "createReminder:failure: " + error);
                    Toast.makeText(AddReminderActivity.this, "Data failed: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back_addreminder) {
            Intent intent = new Intent(AddReminderActivity.this, ReminderActivity.class);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.btn_create_reminder){
            createNewReminder();
        }
    }

    private void createNewReminder(){
        String title = inputTitleReminder.getText().toString().trim();
        String selectedDay = dayReminder.getSelectedItem().toString();
        String selectedTime = tvHourReminder.getText().toString().trim();
        String desc = inputDescReminder.getText().toString().trim();

        if (selectedIconResourceId != 0 && !title.isEmpty() && !selectedDay.isEmpty() && !selectedTime.isEmpty() && !desc.isEmpty()) {
            String timestamp = calculateTimestamp(selectedDay, selectedTime);
            addReminderViewModel.createReminder(title, selectedDay, selectedTime, desc, timestamp, selectedIconResourceId);
            Log.d("AddReminderActivity", "Attempting to schedule reminder");
            ReminderScheduler.scheduleReminder(this, title, desc, timestamp);
            Log.d("AddReminderActivity", "scheduleReminder should have been called");

        } else {
            incompleteFormDialog();
            Log.d("AddReminderActivity", "incomplete");

        }

    }

    private void dataSavedDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.data_saved_dialog, null);

        ImageView toastIcon = layout.findViewById(R.id.img_verif_sent);
        TextView toastText = layout.findViewById(R.id.text_verif_sent);

        String text = "Pengingat Berhasil Ditambahkan";

        toastIcon.setImageResource(R.drawable.ic_checkmark);
        toastText.setText(text);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void incompleteFormDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.incomplete_form_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        dialog.setOnShowListener(dialogInterface -> {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        });

        dialog.show();

        Button btnClose = dialogView.findViewById(R.id.btn_close_incomplete);
        btnClose.setOnClickListener(v -> dialog.dismiss());
    }

    public void dialogIconReminder (Button btnIconReminder) {
        btnIconReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IconReminderFragment iconReminderFragment = new IconReminderFragment();
                iconReminderFragment.show(getSupportFragmentManager(), "IconReminderDialog");
                iconReminderFragment.setOnIconSelectedListener(AddReminderActivity.this);
            }
        });
    }
    @Override
    public void onIconSelected(int iconResId) {
        imgIconReminder.setImageResource(iconResId);
        selectedIconResourceId = iconResId;
    }

    private void setTimePicker(TextView tvHourReminder) {
        tvHourReminder.setOnClickListener(new View.OnClickListener() {
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
                tvHourReminder.setText(selectedTime);
            }
        });

        materialTimePicker.show(getSupportFragmentManager(), "timePicker");
    }

    private String calculateTimestamp(String selectedDay, String selectedTime) {
        Calendar now = Calendar.getInstance();
        String[] timeParts = selectedTime.split(":");

        if (timeParts.length != 2) {
            return "";
        }

        int hour = 0;
        int minute = 0;

        try {
            hour = Integer.parseInt(timeParts[0]);
            minute = Integer.parseInt(timeParts[1]);
        } catch (NumberFormatException e) {
            return "";
        }

        int dayOfWeekNow = now.get(Calendar.DAY_OF_WEEK);
        int targetDayOfWeek = getDayOfWeek(selectedDay);
        if (targetDayOfWeek == -1) {
            return "";
        }
        int dayDifference = (targetDayOfWeek - dayOfWeekNow + 7) % 7;

        Calendar targetDate = (Calendar) now.clone();
        targetDate.add(Calendar.DAY_OF_MONTH, dayDifference);
        targetDate.set(Calendar.HOUR_OF_DAY, hour);
        targetDate.set(Calendar.MINUTE, minute);
        targetDate.set(Calendar.SECOND, 0);
        targetDate.set(Calendar.MILLISECOND, 0);

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
                return -1;
        }
    }

}