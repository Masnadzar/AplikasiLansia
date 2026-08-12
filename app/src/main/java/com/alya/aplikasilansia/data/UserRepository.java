package com.alya.aplikasilansia.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
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
    private FirebaseFirestore db; // DIUBAH: dari DatabaseReference (RTDB) -> FirebaseFirestore
    private StorageReference mStorage;

    public UserRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // DIUBAH: FirebaseDatabase.getInstance().getReference()
        mStorage = FirebaseStorage.getInstance().getReference("profile_images");
    }

    public MutableLiveData<User> fetchUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();

            // DIUBAH: DatabaseReference.child("users").child(uid) -> collection("users").document(uid)
            db.collection("users").document(firebaseUser.getUid())
                    .get() // DIUBAH: addListenerForSingleValueEvent -> get() (sekali ambil, bukan listener realtime)
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String birthDate = snapshot.getString("birthDate");
                            String userName = snapshot.getString("userName");
                            String gender = snapshot.getString("gender");
                            String imageUrl = snapshot.getString("profileImageUrl");
                            String caregiver = snapshot.getString("caregiver");
                            String maritalStatus = snapshot.getString("maritalStatus");

                            // DIUBAH: ambil medHistory sebagai List<Map>, lalu konversi manual ke inputMedHistory
                            // (Firestore tidak punya konsep "children" seperti RTDB, medHistory disimpan sebagai array)
                            List<inputMedHistory> medHistory = new ArrayList<>();
                            List<Map<String, Object>> rawMedHistory =
                                    (List<Map<String, Object>>) snapshot.get("medHistory");
                            if (rawMedHistory != null) {
                                for (Map<String, Object> item : rawMedHistory) {
                                    String penyakit = (String) item.get("penyakit");
                                    String lamanya = (String) item.get("lamanya");
                                    String lamanyaBulan = (String) item.get("lamanyaBulan");
                                    medHistory.add(new inputMedHistory(penyakit, lamanya, lamanyaBulan));
                                }
                            }

                            // profileImageUrl sekarang String, bukan Uri lagi -> tidak perlu Uri.parse()
                            User userProfile = new User(email, birthDate, userName, gender, imageUrl, caregiver, maritalStatus, medHistory);
                            userLiveData.setValue(userProfile);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // DIUBAH: onCancelled -> addOnFailureListener
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

                            // DIUBAH: mDatabase.child("users").child(uid).setValue(obj)
                            //      -> collection("users").document(uid).set(obj)
                            db.collection("users").document(user.getUid()).set(additionalUserInfo);
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

            db.collection("users").document(user.getUid()).set(additionalUserInfo)
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

    public void updateProfile(String newUserName, String email, String birthDate, Uri profileImageUri, MutableLiveData<String> updateResultLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            // DIUBAH: DatabaseReference userRef -> DocumentReference userRef
            Map<String, Object> updates = new HashMap<>();
            if (newUserName != null) updates.put("userName", newUserName);
            if (email != null) updates.put("email", email);
            if (birthDate != null) updates.put("birthDate", birthDate);

            // DIUBAH: userRef.updateChildren(updates) -> userRef.update(updates)
            db.collection("users").document(firebaseUser.getUid())
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

    // Method upload foto profil ke Firebase Storage -> TIDAK ADA PERUBAHAN
    // (Storage tetap sama persis, tidak terpengaruh migrasi RTDB->Firestore)
    private void uploadProfileImage(Uri imageUri, String userId, MutableLiveData<String> imageUrlLiveData) {
        StorageReference profileImageRef = mStorage.child(userId + ".jpg");

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // DIUBAH: update field profileImageUrl di Firestore, bukan RTDB
                        db.collection("users").document(userId)
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
            // DIUBAH: child("medHistory").setValue(list) -> update("medHistory", list)
            // Karena medHistory cuma 1 field (array), pakai update() field tunggal lebih tepat
            // daripada .document(uid).collection("medHistory") yang berarti subcollection (beda konsep)
            db.collection("users").document(firebaseUser.getUid())
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

            db.collection("users").document(firebaseUser.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> updateResultLiveData.postValue("Medical data updated successfully"))
                    .addOnFailureListener(e -> updateResultLiveData.postValue("Failed to update medical data: " + e.getMessage()));
        } else {
            updateResultLiveData.postValue("User not authenticated");
        }
    }

}