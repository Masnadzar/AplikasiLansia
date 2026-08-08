package com.alya.aplikasilansia.ui.editprofile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.inputMedHistory;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class EditProfileActivity extends AppCompatActivity implements OnSaveEditListener{

    private static final int REQUEST_PICK_IMAGE = 1;
    private EditProfileViewModel editProfileViewModel;
    private ImageView imageViewProfile;
    private Uri selectedImageUri;
    private TextView personalProfile, healthProfile, userNameTextView;
    private RelativeLayout editProfileImg;
    String fragmentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editProfileViewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        fragmentType = getIntent().getStringExtra("FRAGMENT_TYPE");

        personalProfile = findViewById(R.id.btnPersonalData);
        healthProfile = findViewById(R.id.btnHealthData);
        userNameTextView = findViewById(R.id.profile_userName);
        editProfileImg = findViewById(R.id.profile_image_edit);
        imageViewProfile = findViewById(R.id.edit_profile_image);

        if (savedInstanceState == null & fragmentType != null) {
            if (fragmentType.equals("personal")) {
                replaceFragment(new EditPersonalFragment());
                personalProfile.setBackgroundResource(R.drawable.text_blue_underlined);
                healthProfile.setBackgroundResource(R.drawable.text_transparant);
            } else if (fragmentType.equals("health")) {
                Log.e("EditProfileActivity", "Fragment type HEALTH provided");

                replaceFragment(new EditHealthFragment());
                healthProfile.setBackgroundResource(R.drawable.text_blue_underlined);
                personalProfile.setBackgroundResource(R.drawable.text_transparant);
            }
        } else if (fragmentType == null){
            Log.e("EditProfileActivity", "Fragment type not provided");
            Toast.makeText(this,"Fragment type not provided: " + fragmentType, Toast.LENGTH_SHORT).show();
        }

        editProfileImg.setOnClickListener(v -> openGallery());

        editProfileViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                userNameTextView.setText(user.getUserName());
                if (user.getProfileImageUrl() != null) {
                    Glide.with(EditProfileActivity.this)
                            .load(user.getProfileImageUrl())
                            .into(imageViewProfile);
                } else {
                    imageViewProfile.setImageResource(R.drawable.img);
                }
            }
        });

        editProfileViewModel.getUpdateResultLiveData().observe(this, updateResult -> {
            if (updateResult.equals("Profile updated successfully")) {
                dataSavedDialog();
                finish();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Failed to update profile", Snackbar.LENGTH_LONG)
                        .setAction("Retry", v -> saveProfileChanges())
                        .show();
            }
        });

        editProfileViewModel.fetchUser();
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

    @Override
    public void onSavePersonalData(String newUsername, String newBirthdate) {
        fragmentType = "personal";
        editProfileViewModel.updateProfile(newUsername, null, newBirthdate, selectedImageUri);
    }

    @Override
    public void onSaveHealthData(String newCaregiver, String newStatus, List<inputMedHistory> medHistoryList) {
        fragmentType = "health";

        editProfileViewModel.updateHealthData2(newCaregiver, newStatus);
        editProfileViewModel.updateMedRecord(medHistoryList);
        saveProfileChanges();
    }

    private void saveProfileChanges() {
        editProfileViewModel.updateProfile(null, null, null, selectedImageUri);
        finish();
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
            imageViewProfile.setImageURI(selectedImageUri);
            Log.d("EditProfileActivity", "Selected image URI: " + selectedImageUri.toString());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.edit_profile_frame, fragment);
        transaction.commit();
    }
}
