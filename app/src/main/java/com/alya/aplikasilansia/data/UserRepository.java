package com.alya.aplikasilansia.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.alya.aplikasilansia.AppApplication;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
// DIHAPUS: import com.google.firebase.storage.FirebaseStorage / StorageReference
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    // DIHAPUS: private StorageReference mStorage;
    // Cloudinary tidak butuh reference object seperti Storage -- upload langsung lewat MediaManager (static/singleton)

    public UserRepository() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        // DIHAPUS: mStorage = FirebaseStorage.getInstance().getReference("profile_images");
    }

    public MutableLiveData<User> fetchUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            mFirestore.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String birthDate = snapshot.getString("birthDate");
                            String userName = snapshot.getString("userName");
                            String gender = snapshot.getString("gender");
                            String imageUrl = snapshot.getString("profileImageUrl");
                            String caregiver = snapshot.getString("caregiver");
                            String maritalStatus = snapshot.getString("maritalStatus");

                            List<inputMedHistory> medHistory = new ArrayList<>();
                            List<Map<String, Object>> rawMedHistory =
                                    (List<Map<String, Object>>) snapshot.get("medHistory");
                            if (rawMedHistory != null) {
                                for (Map<String, Object> item : rawMedHistory) {
                                    inputMedHistory med = new inputMedHistory();
                                    if (item.get("lamanya") != null) med.setLamanya(String.valueOf(item.get("lamanya")));
                                    if (item.get("lamanyaBulan") != null) med.setLamanyaBulan(String.valueOf(item.get("lamanyaBulan")));
                                    if (item.get("penyakit") != null) med.setPenyakit(String.valueOf(item.get("penyakit")));
                                    medHistory.add(med);
                                }
                            }

                            Uri profileImageUri = (imageUrl != null) ? Uri.parse(imageUrl) : null;

                            User userProfile = new User(firebaseUser.getEmail(), birthDate, userName, gender, profileImageUri, caregiver, maritalStatus, medHistory);
                            userLiveData.setValue(userProfile);
                        } else {
                            userLiveData.setValue(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserRepository", "Firestore error: ", e);
                        userLiveData.setValue(null);
                    });
        }

        return userLiveData;
    }

    public void register(String email, String password, String birthDate, String userName, String gender, String caregiver, String maritalStatus, List<inputMedHistory> medHistory, MutableLiveData<FirebaseUser> userLiveData, MutableLiveData<String> errorLiveData) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            User additionalUserInfo = new User(email, birthDate, userName, gender, null, caregiver, maritalStatus, medHistory);
                            mFirestore.collection("users").document(user.getUid())
                                    .set(additionalUserInfo);
                            userLiveData.postValue(user);
                        }
                    } else {
                        errorLiveData.postValue(task.getException().getMessage());
                    }
                });
    }

    public void registerWithGoogle(GoogleSignInAccount account, String birthDate, String caregiver, String maritalStatus, List<inputMedHistory> medHistory, MutableLiveData<FirebaseUser> userLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            User additionalUserInfo = new User(
                    account.getEmail(),
                    birthDate,
                    account.getDisplayName(),
                    null,
                    null,
                    null,
                    null,
                    null
            );

            mFirestore.collection("users").document(user.getUid())
                    .set(additionalUserInfo)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userLiveData.postValue(user);
                        } else {
                            errorLiveData.postValue("Failed to save user info: " + task.getException().getMessage());
                        }
                    });
        } else {
            errorLiveData.postValue("User is not signed in.");
        }
    }

    public void login(String email, String password, MutableLiveData<FirebaseUser> userLiveData, MutableLiveData<String> errorLiveData) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        userLiveData.postValue(user);
                    } else {
                        errorLiveData.postValue(getFirebaseAuthErrorMessage(task.getException()));
                    }
                });
    }

    private String getFirebaseAuthErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            return "Pengguna tidak ditemukan. Silakan periksa email Anda atau daftar terlebih dahulu.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Data tidak valid. Silahkan periksa email dan kata sandi Anda.";
        } else {
            return "Gagal masuk. Silakan coba lagi.";
        }
    }

    public void signOut() {
        mAuth.signOut();
    }

    /**
     * Menghapus akun pengguna secara permanen: menghapus subcollection
     * (quizResults, bloodPressure, reminders), dokumen users/{uid}, foto profil
     * di Storage, lalu akun di Firebase Authentication.
     *
     * Jika sesi login sudah tidak "recent", Firebase akan menolak firebaseUser.delete()
     * dengan FirebaseAuthRecentLoginRequiredException. Di kasus ini, deleteResultLiveData
     * akan diisi string "RECENT_LOGIN_REQUIRED" -- UI harus menangkap nilai ini dan meminta
     * user re-login (email/password atau Google Sign-In ulang) sebelum memanggil
     * deleteAccount() lagi.
     */
    public void deleteAccount(MutableLiveData<String> deleteResultLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            deleteResultLiveData.postValue("User not authenticated");
            return;
        }

        String uid = firebaseUser.getUid();

        deleteSubcollection(uid, "quizResults", () ->
                deleteSubcollection(uid, "bloodPressure", () ->
                        deleteSubcollection(uid, "reminders", () -> {

                            mFirestore.collection("users").document(uid)
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            // DIHAPUS: mStorage.child(uid + ".jpg").delete()
                                            // Cloudinary menghapus file WAJIB pakai signed request
                                            // (butuh API Secret) -- API Secret TIDAK BOLEH ditaruh
                                            // di kode Android (bisa diekstrak siapa saja dari APK).
                                            // Solusi aman: hapus file Cloudinary lewat backend
                                            // (Cloud Function) yang menyimpan API Secret dengan aman,
                                            // dipicu dari sini via HTTP call ke endpoint backend tsb.
                                            // Sementara: foto lama dibiarkan "yatim" di Cloudinary
                                            // (tidak terhubung user manapun lagi, tidak masalah secara
                                            // fungsional, cuma numpuk storage kalau tidak dibersihkan berkala).
                                            deleteFirebaseAuthAccount(firebaseUser, deleteResultLiveData))
                                    .addOnFailureListener(e ->
                                            deleteResultLiveData.postValue("Failed to delete user data: " + e.getMessage()));
                        })));
    }

    private void deleteSubcollection(String uid, String subcollectionName, Runnable onComplete) {
        mFirestore.collection("users").document(uid).collection(subcollectionName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Failed to delete subcollection " + subcollectionName + ": " + e.getMessage());
                    onComplete.run(); // tetap lanjut walau gagal, jangan blokir proses hapus akun
                });
    }

    private void deleteFirebaseAuthAccount(FirebaseUser firebaseUser, MutableLiveData<String> deleteResultLiveData) {
        firebaseUser.delete()
                .addOnSuccessListener(aVoid -> deleteResultLiveData.postValue("Account deleted successfully"))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                        deleteResultLiveData.postValue("RECENT_LOGIN_REQUIRED");
                    } else {
                        deleteResultLiveData.postValue("Failed to delete account: " + e.getMessage());
                    }
                });
    }

    public void updateProfile(String newUserName, String email, String birthDate, Uri profileImageUri, MutableLiveData<String> updateResultLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        Log.d("UserRepository", "updateProfile() called. currentUser=" + (firebaseUser != null ? firebaseUser.getUid() : "NULL") + ", profileImageUri=" + profileImageUri);

        if (firebaseUser == null) {
            // BARU: SEBELUMNYA kalau firebaseUser null, method ini diam total -- tidak ada
            // postValue apapun -> observer di Activity TIDAK PERNAH menerima apa-apa ->
            // persis gejala "stuck, tidak ada respon, tidak ada pesan error sama sekali".
            Log.e("UserRepository", "updateProfile() dibatalkan: user tidak login (currentUser null)");
            updateResultLiveData.postValue("Failed to update profile: User not authenticated");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (newUserName != null) updates.put("userName", newUserName);
        if (email != null) updates.put("email", email);
        if (birthDate != null) updates.put("birthDate", birthDate);

        if (updates.isEmpty() && profileImageUri == null) {
            // BARU: Firestore .update() dengan Map KOSONG akan melempar IllegalArgumentException
            // secara SYNCHRONOUS (bukan lewat addOnFailureListener) -- exception ini bisa
            // langsung terlempar ke pemanggil tanpa sempat di-postValue ke LiveData, yang
            // juga bisa terlihat seperti "diam total" kalau tidak ada try-catch di sekitarnya.
            Log.w("UserRepository", "updateProfile() dipanggil tanpa perubahan apapun (updates kosong & tidak ada foto)");
            updateResultLiveData.postValue("Profile updated successfully");
            return;
        }

        if (updates.isEmpty()) {
            // Ada foto tapi field lain semua null -> tetap harus panggil .update() dengan
            // minimal 1 field asli, BUKAN Map kosong. Trik aman: skip .update() field teks,
            // langsung lanjut ke upload foto.
            Log.d("UserRepository", "Tidak ada perubahan field teks, langsung upload foto");
            uploadProfileImage(profileImageUri, firebaseUser.getUid(), updateResultLiveData);
            return;
        }

        Log.d("UserRepository", "Memulai Firestore .update() dengan field: " + updates.keySet());
        mFirestore.collection("users").document(firebaseUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserRepository", "Firestore .update() SUKSES");
                    if (profileImageUri != null) {
                        uploadProfileImage(profileImageUri, firebaseUser.getUid(), updateResultLiveData);
                    } else {
                        updateResultLiveData.postValue("Profile updated successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Firestore .update() GAGAL: " + e.getMessage(), e);
                    updateResultLiveData.postValue("Failed to update profile: " + e.getMessage());
                });
    }

    private void uploadProfileImage(Uri imageUri, String userId, MutableLiveData<String> imageUrlLiveData) {
        Log.d("UserRepository", "uploadProfileImage() dipanggil. uri=" + imageUri + ", userId=" + userId);

        // DIUBAH: putFile() Firebase Storage -> MediaManager.upload() Cloudinary.
        // "public_id" disamakan dengan userId, supaya upload berikutnya dari user yang sama
        // OTOMATIS MENIMPA file lama di Cloudinary (folder profile_images/{userId}), sama
        // seperti perilaku mStorage.child(userId + ".jpg") sebelumnya -- tidak numpuk file lama.
        String requestIdResult;
        try {
            // DIUBAH: public_id sekarang UNIK per upload (userId + timestamp), bukan cuma userId.
            // Cloudinary MELARANG overwrite=true untuk unsigned preset SECARA MUTLAK, baik
            // lewat parameter kode MAUPUN lewat toggle di Dashboard preset -- ini pembatasan
            // keamanan permanen dari Cloudinary, tidak bisa diakali dengan cara apapun selama
            // masih pakai unsigned upload. Solusinya: setiap upload bikin FILE BARU dengan nama
            // berbeda (bukan menimpa file lama). Efek sampingnya URL foto SELALU BEDA tiap
            // upload -> otomatis menyelesaikan juga masalah cache Glide (URL beda = Glide pasti
            // fetch ulang dari jaringan, tidak mungkin kepakai cache foto lama).
            String uniquePublicId = userId + "_" + System.currentTimeMillis();
            requestIdResult = MediaManager.get().upload(imageUri)
                    .unsigned(AppApplication.CLOUDINARY_UPLOAD_PRESET) // DIUBAH: hardcode "profile_upload" -> referensi konstanta di AppApplication (satu sumber, tidak duplikat)
                    .option("public_id", uniquePublicId)
                    .option("folder", "profile_images")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            // BARU: log ini WAJIB muncul kalau job benar-benar mulai diproses
                            // WorkManager. Kalau log ini TIDAK PERNAH muncul di Logcat,
                            // artinya macetnya di WorkManager/scheduling, bukan di jaringan.
                            Log.d("UserRepository", "Cloudinary onStart: requestId=" + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            Log.d("UserRepository", "Cloudinary onProgress: " + bytes + "/" + totalBytes);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Log.d("UserRepository", "Cloudinary onSuccess: " + resultData);
                            String imageUrl = (String) resultData.get("secure_url"); // DIUBAH: uri.toString() -> ambil "secure_url" dari respons Cloudinary

                            mFirestore.collection("users").document(userId)
                                    .update("profileImageUrl", imageUrl)
                                    // DIUBAH: SEBELUMNYA post URL Cloudinary asli ke imageUrlLiveData,
                                    // padahal LiveData yang sama ini dibaca EditProfileActivity sebagai
                                    // PESAN STATUS ("Profile updated successfully"), bukan sebagai URL.
                                    // Akibatnya kondisi `"Profile updated successfully".equals(updateResult)`
                                    // SELALU FALSE setiap kali user ganti foto -> observer tidak pernah
                                    // menganggap ini "sukses" -> finish() tidak pernah terpanggil ->
                                    // layar "stuck" di situ terus walau upload sebenarnya BERHASIL.
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("UserRepository", "Firestore field profileImageUrl berhasil diupdate: " + imageUrl);
                                        imageUrlLiveData.postValue("Profile updated successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("UserRepository", "Failed to update profile image URL: " + e.getMessage(), e);
                                        imageUrlLiveData.postValue("Failed to update profile: " + e.getMessage());
                                    });
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            // BARU: log lengkap termasuk kode error dari Cloudinary,
                            // supaya kelihatan apakah ini soal preset salah, cloud_name salah,
                            // atau soal jaringan.
                            Log.e("UserRepository", "Cloudinary onError: code=" + error.getCode() + ", desc=" + error.getDescription());
                            imageUrlLiveData.postValue("Failed to update profile: " + error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            // BARU: ini kondisi PALING MUNGKIN jadi penyebab "diam total tanpa error".
                            // onReschedule dipanggil kalau WorkManager MENUNDA job (biasanya karena
                            // tidak ada koneksi internet saat itu) -- job TIDAK gagal, TIDAK sukses,
                            // cuma "nunggu" tanpa batas waktu sampai syarat terpenuhi (misal internet
                            // nyala lagi). Dari sisi observer, ini terlihat PERSIS seperti "stuck diam".
                            Log.w("UserRepository", "Cloudinary onReschedule (job ditunda, biasanya karena TIDAK ADA KONEKSI INTERNET saat upload): " + error.getDescription());
                            imageUrlLiveData.postValue("Failed to update profile: Tidak ada koneksi internet, coba lagi");
                        }
                    })
                    .dispatch();
            Log.d("UserRepository", "MediaManager.dispatch() dipanggil, requestId=" + requestIdResult);
        } catch (Exception e) {
            // BARU: kalau MediaManager belum ter-init dengan benar (misal AppApplication
            // gagal dipanggil / cloud_name kosong), .upload() bisa melempar exception
            // SYNCHRONOUS di sini -- tanpa try-catch ini, exception itu bisa membuat
            // seluruh chain berhenti tanpa pernah sampai ke callback apapun ("diam total").
            Log.e("UserRepository", "Exception saat memanggil MediaManager.upload(): " + e.getMessage(), e);
            imageUrlLiveData.postValue("Failed to update profile: " + e.getMessage());
        }
    }

    public void updateMedHistory(List<inputMedHistory> newMedHistory, MutableLiveData<String> updateResultLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            mFirestore.collection("users").document(firebaseUser.getUid())
                    .update("medHistory", newMedHistory)
                    .addOnSuccessListener(aVoid -> updateResultLiveData.postValue("Medical history updated successfully"))
                    .addOnFailureListener(e -> updateResultLiveData.postValue("Failed to update medical history: " + e.getMessage()));
        } else {
            updateResultLiveData.postValue("User not authenticated");
        }
    }

    public void updateMedData(String caregiver, String maritalStatus, MutableLiveData<String> updateResultLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("caregiver", caregiver);
            updates.put("maritalStatus", maritalStatus);

            mFirestore.collection("users").document(firebaseUser.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> updateResultLiveData.postValue("Medical data updated successfully"))
                    .addOnFailureListener(e -> updateResultLiveData.postValue("Failed to update medical data: " + e.getMessage()));
        } else {
            updateResultLiveData.postValue("User not authenticated");
        }
    }

}