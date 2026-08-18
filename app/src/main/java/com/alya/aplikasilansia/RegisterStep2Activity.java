package com.alya.aplikasilansia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.data.UserData;
import com.alya.aplikasilansia.data.inputMedHistory;
import com.alya.aplikasilansia.ui.editprofile.EditProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class RegisterStep2Activity extends AppCompatActivity {

    private EditProfileViewModel editProfileViewModel;
    private List<inputMedHistory> inputDataList = new ArrayList<>();
    private static final int REQUEST_PICK_IMAGE = 1;
    private Button buttonAddInput, buttonSave, buttonNext;
    private LinearLayout viewMedRecord;
    private ImageView profileImage;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step2);

        buttonAddInput = findViewById(R.id.btn_add_medhistory);
        buttonSave = findViewById(R.id.btn_save_medhistory);
        buttonNext = findViewById(R.id.btn_next_medhistory);
        viewMedRecord = findViewById(R.id.view_med_layout);

        editProfileViewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        MedicalRecordFragment medicalRecordFragment = new MedicalRecordFragment();
        inputDataList = medicalRecordFragment.inputDataList;

        buttonAddInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogMedRecord();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register3 = new Intent(RegisterStep2Activity.this, RegisterStep3Activity.class);
                startActivity(register3);
                finish();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RegisterStep2Activity", "data saved");
                if (inputDataList != null && !inputDataList.isEmpty()) {
                    UserData userData = UserData.getInstance();
                    userData.setMedHistory(inputDataList);
                    userData.setProfileImageUrl(selectedImageUri);
                    Log.d("RegisterStep2Activity", "profile image to send: " +selectedImageUri);

                    Intent register3 = new Intent(RegisterStep2Activity.this, RegisterStep3Activity.class);
                    startActivity(register3);
                    finish();

                } else {
                    IncompleteFormDialog dialog = new IncompleteFormDialog();
                    dialog.show(getSupportFragmentManager(), "IncompleteFormDialog");
                }
            }
        });

        editProfileViewModel.fetchUser();
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImage.setImageURI(selectedImageUri);
            Log.d("RegisterStep2Activity", "profile image: " +selectedImageUri);

        }
    }

    private void viewMedicalRecord() {
        viewMedRecord.removeAllViews();
        if (inputDataList != null && !inputDataList.isEmpty()) {
            for (inputMedHistory history : inputDataList) {
                View itemView = getLayoutInflater().inflate(R.layout.register_view_medhistory, null);

                TextView tvPenyakit = itemView.findViewById(R.id.tv_penyakit);
                TextView tvMedYears = itemView.findViewById(R.id.tv_med_years);
                TextView tvMedMonths = itemView.findViewById(R.id.tv_med_months);

                tvPenyakit.setText(history.getPenyakit());
                tvMedYears.setText(history.getLamanya());
                tvMedMonths.setText(history.getLamanyaBulan());

                viewMedRecord.addView(itemView);
            }
        } else {
            Toast.makeText(this, "No medical history data available", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialogMedRecord () {
        MedicalRecordFragment medicalRecordFragment = new MedicalRecordFragment();
        medicalRecordFragment.setOnDataSavedListener(new MedicalRecordFragment.OnDataSavedListener() {
            @Override
            public void onDataSaved(List<inputMedHistory> updatedList) {
                inputDataList.addAll(updatedList);
                Log.d("RegisterStep2Activity", "Updated inputDataList: " + inputDataList.toString());
                viewMedicalRecord();
            }
        });

        medicalRecordFragment.show(getSupportFragmentManager(), "MedicalRecordDialog");
    }
}