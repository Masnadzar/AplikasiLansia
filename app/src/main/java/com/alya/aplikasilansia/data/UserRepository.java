package com.alya.aplikasilansia.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private StorageReference mStorage; // Firebase Storage reference (tetap, tidak dimigrasi)

    public UserRepository() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference("profile_images"); // Storage reference
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
                                            mStorage.child(uid + ".jpg").delete()
                                                    .addOnCompleteListener(storageTask ->
                                                            deleteFirebaseAuthAccount(firebaseUser, deleteResultLiveData)))
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

        if (firebaseUser != null) {
            Map<String, Object> updates = new HashMap<>();
            if (newUserName != null) updates.put("userName", newUserName);
            if (email != null) updates.put("email", email);
            if (birthDate != null) updates.put("birthDate", birthDate);

            mFirestore.collection("users").document(firebaseUser.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (profileImageUri != null) {
                            uploadProfileImage(profileImageUri, firebaseUser.getUid(), updateResultLiveData);
                        } else {
                            updateResultLiveData.postValue("Profile updated successfully");
                        }
                    })
                    .addOnFailureListener(e -> {
                        updateResultLiveData.postValue("Failed to update profile: " + e.getMessage());
                    });
        }
    }

    private void uploadProfileImage(Uri imageUri, String userId, MutableLiveData<String> imageUrlLiveData) {
        StorageReference profileImageRef = mStorage.child(userId + ".jpg");

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        mFirestore.collection("users").document(userId)
                                .update("profileImageUrl", imageUrl)
                                .addOnSuccessListener(aVoid -> imageUrlLiveData.postValue(imageUrl))
                                .addOnFailureListener(e -> Log.e("UserRepository", "Failed to update profile image URL: " + e.getMessage()));
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRepository", "Failed to upload profile image: " + e.getMessage());
                });
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