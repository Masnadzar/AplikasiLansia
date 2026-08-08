package com.alya.aplikasilansia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.data.UserData;
import com.alya.aplikasilansia.data.inputMedHistory;
import com.alya.aplikasilansia.ui.reminder.CustomSpinnerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Objects;

public class RegisterStep3Activity extends AppCompatActivity {
    private RegisterViewModel registerViewModel;
    private List<inputMedHistory> medHistory;
    private String careGiver, maritalStat;
    private Uri profileImageUrl;
    private Spinner spinnerCareGiver, spinnerMaritalStatus;
    private Button btnSaveMedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step3);

        spinnerCareGiver = findViewById(R.id.spinner_care_giver);
        spinnerMaritalStatus = findViewById(R.id.spinner_marital);
        btnSaveMedData = findViewById(R.id.btn_save_med);

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        UserData userData = UserData.getInstance();

        String email = userData.getEmail();
        String password = userData.getPassword();
        String birthDate = userData.getBirthDate();
        String userName = userData.getUserName();
        String gender = userData.getGender();
        profileImageUrl = userData.getProfileImageUrl();
        medHistory = userData.getMedHistory();
        Log.d("RegisterStep3Activity", "profile image to save: " +profileImageUrl);

        btnSaveMedData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                careGiver = spinnerCareGiver.getSelectedItem().toString();
                maritalStat = spinnerMaritalStatus.getSelectedItem().toString();

                if (careGiver.equals("--Pilih yang Merawat--") || maritalStat.equals("--Pilih Status--")) {
                    incompleteFormDialog();
                    return;
                }

                if (email == null || password == null || birthDate == null || userName == null || gender == null) {
                    Log.d("RegisterStep3Activity", "Error: One or more required fields are null.");
                    return;
                }

                registerViewModel.register(email, password, birthDate, userName, gender);

                observeData();
            }
        });

        CustomSpinnerAdapter careGiverAdapter = new CustomSpinnerAdapter(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.care_giver)
        );
        careGiverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        CustomSpinnerAdapter maritalStatusAdapter = new CustomSpinnerAdapter(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.marital_status)
        );
        maritalStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCareGiver.setAdapter(careGiverAdapter);
        spinnerMaritalStatus.setAdapter(maritalStatusAdapter);
    }

    private void observeData() {
        registerViewModel.userLiveData.observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser != null) {
                    Log.d("RegisterStep3Activity", "createUserWithEmail:success");
                    sendEmailVerification(firebaseUser);
                    registerHealthData();
                }
            }
        });

        registerViewModel.errorLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null) {
                    Log.w("RegisterStep3Activity", "createUserWithEmail:failure: " + error);
                    Toast.makeText(RegisterStep3Activity.this, "Authentication failed: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerHealthData(){
        registerViewModel.registerHealth1(medHistory);
        registerViewModel.registerHealth2(careGiver, maritalStat);
    }
    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            verificationSentDialog(user.getEmail());

                        } else {
                            Log.e("RegisterStep3Activity", "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterStep3Activity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void verificationSentDialog(String emailSent){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.email_verif_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 80% of screen width
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; // Adjust height as needed
        dialog.getWindow().setAttributes(params);

        Button btnLogin = dialogView.findViewById(R.id.btn_close_verif);
        TextView emailVerifDialogTv = dialogView.findViewById(R.id.tv_email_verif);
        emailVerifDialogTv.setText(emailSent);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intentReg2 = new Intent(RegisterStep3Activity.this, LoginActivity.class);
                startActivity(intentReg2);
                finish();
            }
        });
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
}