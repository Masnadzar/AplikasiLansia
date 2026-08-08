package com.alya.aplikasilansia.ui.bloodpressure;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.LoginActivity;
import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.BloodPressure;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BloodPressureActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private MaterialButton btnBackBP, btnSaveBP;
    private TextView tvDatePickerBP;
    private EditText etBP, etPulse;
    private BloodPressureAdapter adapter;
    private BloodPresViewModel bloodPresViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_pressure);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // User is not logged in, redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close RestrictedActivity

        } else {
            btnBackBP = findViewById(R.id.btn_back_bp);
            setBtnBackBP(btnBackBP);

            tvDatePickerBP = findViewById(R.id.tv_date_bp);
            setTvDatePickerBP(tvDatePickerBP);
            btnSaveBP = findViewById(R.id.btn_save_bp);
            etBP = findViewById(R.id.etBloodPressure);
            etPulse = findViewById(R.id.etPulse);

            // Initialize the ViewModel
            bloodPresViewModel = new ViewModelProvider(this).get(BloodPresViewModel.class);

            // Set up the RecyclerView
            setUpBloodPressureRV();
            // Fetch blood pressure data
            bloodPresViewModel.fetchBloodPressureData();

            // Observe the blood pressure data

            bloodPressData();
            // Observe the add blood pressure result
            bloodPresViewModel.getPressureLiveData().observe(this, firebaseUser -> {
                // Successfully added blood pressure, you can handle the success here
                dataSavedDialog();
//                Toast.makeText(BloodPressureActivity.this, "Blood pressure added successfully", Toast.LENGTH_SHORT).show();
                bloodPresViewModel.fetchBloodPressureData();
                bloodPressData();
            });

            // Observe any errors
            bloodPresViewModel.getErrorLiveData().observe(this, error -> {
                Toast.makeText(BloodPressureActivity.this, "error", Toast.LENGTH_SHORT).show();
            });

            btnSaveBP.setOnClickListener(v -> {
                String bloodPressure = etBP.getText().toString().trim();
                String pulse = etPulse.getText().toString().trim();
                String timestamp = tvDatePickerBP.getText().toString().trim();

                if (!bloodPressure.isEmpty() && !pulse.isEmpty() && !timestamp.isEmpty()) {

                    try {
                        int pulseInt = Integer.parseInt(pulse);

                        // Inputs are valid integers, proceed with ViewModel method
                        bloodPresViewModel.addBloodPressure(bloodPressure, String.valueOf(pulseInt), timestamp);

                    } catch (NumberFormatException e) {
                        // Handle case where inputs are not valid integers
                        incompleteFormDialog();
                    }

                } else {
                    // Handle case where inputs are missing
                    incompleteFormDialog();
                }
            });
        }
    }

    public void bloodPressData() {
        bloodPresViewModel.getBloodPressureData().observe(this, new Observer<List<BloodPressure>>() {
            @Override
            public void onChanged(List<BloodPressure> bloodPressures) {
                // Update the adapter's data
                adapter.updateList(bloodPressures);
//                adapter.setBloodPressureList(bloodPressures);
            }

        });
    }

    private void dataSavedDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.data_saved_dialog, null);

        ImageView toastIcon = layout.findViewById(R.id.img_verif_sent);
        TextView toastText = layout.findViewById(R.id.text_verif_sent);

        String text = "Data Berhasil Disimpan";
        toastIcon.setImageResource(R.drawable.ic_checkmark);
        toastText.setText(text);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void incompleteFormDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.incomplete_form_dialog, null);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Set the custom background drawable with rounded corners
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        // Adjust dialog size programmatically after showing it
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 80% of screen width
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; // Adjust height as needed
        dialog.getWindow().setAttributes(params);

        // Get the buttons from the custom layout and set click listeners
        Button btnClose = dialogView.findViewById(R.id.btn_close_incomplete);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void setBtnBackBP (MaterialButton btnBackBP) {
        btnBackBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setTvDatePickerBP (TextView tvDatePickerBP) {
        tvDatePickerBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerBP();
            }
        });
    }

    private void showDatePickerBP() {
        Calendar calendar = Calendar.getInstance();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setOpenAt(calendar.getTimeInMillis());
        constraintsBuilder.setEnd(calendar.getTimeInMillis()); // Set the end constraint to today

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Tanggal Tensi")
                .setCalendarConstraints(constraintsBuilder.build())
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = format.format(selection);
            tvDatePickerBP.setText(formattedDate);
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void setUpBloodPressureRV() {
        RecyclerView recyclerView = findViewById(R.id.rv_bp_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<BloodPressure> bloodPressureList = new ArrayList<>();
        adapter = new BloodPressureAdapter(bloodPressureList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the quiz history data if userId is not null
        bloodPresViewModel.fetchBloodPressureData();
    }
}