package com.alya.aplikasilansia.ui.editprofile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.inputMedHistory;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity implements OnSaveEditListener{

    private static final int REQUEST_PICK_IMAGE = 1;
    private EditProfileViewModel editProfileViewModel;
    private ImageView imageViewProfile;
    private Uri selectedImageUri;
    private TextView personalProfile, healthProfile, userNameTextView;
    private RelativeLayout editProfileImg;
    String fragmentType;

    // BARU: dipakai untuk menyalin file di background thread, supaya tidak nge-block UI
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
            // DIUBAH: "updateResult.equals(...)" -> "\"...\".equals(updateResult)"
            // Urutan dibalik supaya tidak NullPointerException kalau updateResult ternyata null.
            if ("Profile updated successfully".equals(updateResult)) {
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
        // DIHAPUS: finish() yang sebelumnya dipanggil LANGSUNG di sini, tanpa nunggu
        // updateProfile() (Firestore + upload Cloudinary) selesai. Ini penyebab utama
        // "foto tidak tersimpan": Activity ditutup duluan sebelum proses upload beres,
        // kadang bikin proses upload di-background KEPUTUS di tengah jalan (app terasa
        // "keluar" padahal sebenarnya Activity finish lebih cepat dari yang seharusnya).
        // Sekarang cukup panggil updateProfile() saja -- observer updateResultLiveData
        // di onCreate() yang SATU-SATUNYA bertanggung jawab memanggil finish(),
        // dan itu baru terjadi SETELAH proses benar-benar selesai (berhasil/gagal).
        editProfileViewModel.updateProfile(null, null, null, selectedImageUri);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri pickedUri = data.getData();
            if (pickedUri == null) return;

            Log.d("EditProfileActivity", "Picked image URI: " + pickedUri);

            // DIUBAH: URI hasil pilih (terutama dari Google Photos) cuma dapat izin baca
            // SEMENTARA, hanya valid selagi Activity ini masih hidup. Cloudinary upload lewat
            // WorkManager (proses BACKGROUND, bisa jalan belakangan/lintas restart app) --
            // begitu WorkManager baru baca file-nya nanti, izin tadi sudah keburu hangus ->
            // SecurityException: Permission Denial (persis error yang dilaporkan).
            // Solusi: SALIN isi file ke penyimpanan lokal app SAAT INI JUGA (selagi izin
            // masih valid), lalu upload file lokal itu -- bukan URI Google Photos aslinya.
            copyToLocalFileAndPreview(pickedUri);
        }
    }

    private void copyToLocalFileAndPreview(Uri sourceUri) {
        executorService.execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(sourceUri);
                if (inputStream == null) {
                    Log.e("EditProfileActivity", "Failed to open input stream for " + sourceUri);
                    return;
                }

                File localFile = new File(getCacheDir(), "profile_image_" + System.currentTimeMillis() + ".jpg");
                try (FileOutputStream outputStream = new FileOutputStream(localFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                inputStream.close();

                Uri localUri = Uri.fromFile(localFile);
                Log.d("EditProfileActivity", "Copied to local file: " + localUri);

                // Balik ke main thread untuk update UI & simpan referensi URI lokal
                mainHandler.post(() -> {
                    selectedImageUri = localUri; // DIUBAH: yang di-upload sekarang URI file LOKAL, bukan URI Google Photos
                    Glide.with(EditProfileActivity.this)
                            .load(selectedImageUri)
                            .into(imageViewProfile);
                });
            } catch (IOException e) {
                Log.e("EditProfileActivity", "Failed to copy picked image to local file: " + e.getMessage());
                mainHandler.post(() ->
                        Snackbar.make(findViewById(android.R.id.content), "Gagal memproses foto, coba pilih foto lain", Snackbar.LENGTH_LONG).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown(); // BARU: hentikan background thread saat Activity ditutup, cegah memory leak
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.edit_profile_frame, fragment);
        transaction.commit();
    }
}