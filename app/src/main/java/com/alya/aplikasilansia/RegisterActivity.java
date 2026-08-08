package com.alya.aplikasilansia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.data.UserData;
import com.alya.aplikasilansia.ui.reminder.CustomSpinnerAdapter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private RegisterViewModel registerViewModel;
    private android.text.InputType InputType;
    private EditText passwordInputReg, nameInputReg, emailInputReg;
    private ImageView viewPasswordBtn;
    private TextView birthdateTextView, emailVerifDialogTv;
    private Spinner spinnerGender;
    private FirebaseAuth mAuth;
    private String selectedGender, email;
    private boolean isDialogClosed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        birthdateTextView = findViewById(R.id.tv_birthDateReg);
        nameInputReg = findViewById(R.id.name_input_reg);
        emailInputReg = findViewById(R.id.email_input_reg);
        birthdateTextView = findViewById(R.id.tv_birthDateReg);
        spinnerGender = findViewById(R.id.spinner_gender);
        Button registerBtn = findViewById(R.id.btn_regis);

        setPasswordToggle();
        loginFromRegister();
        setupDatePicker(birthdateTextView);

        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.gender_array)
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(spinnerAdapter);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailInputReg.getText().toString().trim();
                String password = passwordInputReg.getText().toString().trim();
                String birthDate = birthdateTextView.getText().toString().trim();
                String userName = nameInputReg.getText().toString().trim();
                selectedGender = spinnerGender.getSelectedItem().toString();

                if (email.isEmpty() || password.isEmpty() || birthDate.isEmpty() || userName.isEmpty() || selectedGender.equals("Jenis Kelamin") || selectedGender.isEmpty()) {
                    incompleteFormDialog();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    String text = "Email tidak valid. Silahkan masukkan email yang valid.";
                    errorMessageToast(text);
                    return;
                }

                if (password.length() < 6) {
                    String text = "Kata sandi harus memiliki setidaknya 6 karakter.";
                    errorMessageToast(text);
                    return;
                }

                UserData userData = UserData.getInstance();
                userData.setEmail(email);
                userData.setPassword(password);
                userData.setBirthDate(birthDate);
                userData.setUserName(userName);
                userData.setGender(selectedGender);

                Intent intentReg2 = new Intent(RegisterActivity.this, RegisterStep2Activity.class);
                startActivity(intentReg2);
                finish();
            }
        });
    }

    private void loginFromRegister () {
        TextView loginFromRegTv = findViewById(R.id.masukRegTv);
        loginFromRegTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent1);
            }
        });
    }
    private void setPasswordToggle () {
        passwordInputReg = findViewById(R.id.regPassword);
        viewPasswordBtn = findViewById(R.id.view_password_btn);
        viewPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(passwordInputReg, viewPasswordBtn);
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        if (editText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.view_password);
        } else {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.hide_password);
        }
        editText.setSelection(editText.getText().length());
    }

    private void setupDatePicker(TextView birthdateTextView) {
        birthdateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setOpenAt(calendar.getTimeInMillis());
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal Lahir Anda")
                .setCalendarConstraints(constraintsBuilder.build())
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = format.format(selection);
            birthdateTextView.setText(formattedDate);
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void errorMessageToast (String message){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.error_custom_toast, null);

        ImageView toastIcon = layout.findViewById(R.id.img_error_ic);
        TextView toastText = layout.findViewById(R.id.tv_error_message);

        toastIcon.setImageResource(R.drawable.ic_warning); // Set your desired icon here
        toastText.setText(message);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    private void incompleteFormDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.incomplete_form_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialog.show();

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);

        Button btnClose = dialogView.findViewById(R.id.btn_close_incomplete);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}