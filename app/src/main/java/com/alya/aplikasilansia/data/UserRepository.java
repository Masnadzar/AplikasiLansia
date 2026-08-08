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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage; // Firebase Storage reference

    public UserRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference("profile_images"); // Storage reference
    }

    public MutableLiveData<User> fetchUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            DatabaseReference userRef = mDatabase.child("users").child(firebaseUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String birthDate = snapshot.child("birthDate").getValue(String.class);
                        String userName = snapshot.child("userName").getValue(String.class);
                        String gender = snapshot.child("gender").getValue(String.class);
                        String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                        String caregiver = snapshot.child("caregiver").getValue(String.class);
                        String maritalStatus = snapshot.child("maritalStatus").getValue(String.class);

                        // Retrieve medHistory as a List<String>
                        List<inputMedHistory> medHistory = new ArrayList<>();
                        for (DataSnapshot medHistorySnapshot : snapshot.child("medHistory").getChildren()) {
                            medHistory.add(medHistorySnapshot.getValue(inputMedHistory.class));
                        }
                        // Convert string imageUrl to Uri
                        Uri profileImageUri = (imageUrl != null) ? Uri.parse(imageUrl) : null;

                        User userProfile = new User(email, birthDate, userName, gender, profileImageUri, caregiver, maritalStatus, medHistory);
                        userLiveData.setValue(userProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UserRepository", "Database error: ", error.toException());
                    userLiveData.setValue(null); // Optionally set the value to null to indicate a failure
                }
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
                            User additionalUserInfo = new User(email, birthDate, userName, gender,  null, caregiver, maritalStatus, medHistory);
                            mDatabase.child("users").child(user.getUid()).setValue(additionalUserInfo);
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
                    null, // Gender can be fetched from your UI if needed
                    null,
                    null,
                    null,
                    null
            );

            // Save the user data to your database
            mDatabase.child("users").child(user.getUid()).setValue(additionalUserInfo)
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
//                        errorLiveData.postValue(task.getException().getMessage());
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
            DatabaseReference userRef = mDatabase.child("users").child(firebaseUser.getUid());
            Map<String, Object> updates = new HashMap<>();
//            updates.put("userName", newUserName);
//            // Optionally update other fields like email and birthDate
//             updates.put("email", email);
//             updates.put("birthDate", birthDate);
            if (newUserName != null) updates.put("userName", newUserName);
            if (email != null) updates.put("email", email);
            if (birthDate != null) updates.put("birthDate", birthDate);

            // Update profile in Realtime Database
            userRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Upload profile image if URI provided
                        if (profileImageUri != null) {
                            uploadProfileImage(profileImageUri, firebaseUser.getUid(), updateResultLiveData);
                        } else {
                            updateResultLiveData.postValue("Profile updated successfully");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        updateResultLiveData.postValue("Failed to update profile: " + e.getMessage());
                    });
        }
    }
     // Method to upload profile image to Firebase Storage
    private void uploadProfileImage(Uri imageUri, String userId, MutableLiveData<String> imageUrlLiveData) {
        StorageReference profileImageRef = mStorage.child(userId + ".jpg"); // Adjust filename as needed

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Update profile image URL in Realtime Database
                        mDatabase.child("users").child(userId).child("profileImageUrl").setValue(imageUrl)
                                .addOnSuccessListener(aVoid -> imageUrlLiveData.postValue(imageUrl))
                                .addOnFailureListener(e -> Log.e("UserRepository", "Failed to update profile image URL: " + e.getMessage()));
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle image upload failure
                    Log.e("UserRepository", "Failed to upload profile image: " + e.getMessage());
                });
    }

    public void updateMedHistory(List<inputMedHistory> newMedHistory, MutableLiveData<String> updateResultLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            DatabaseReference userRef = mDatabase.child("users").child(firebaseUser.getUid()).child("medHistory");

            userRef.setValue(newMedHistory)
                    .addOnSuccessListener(aVoid -> updateResultLiveData.postValue("Medical history updated successfully"))
                    .addOnFailureListener(e -> updateResultLiveData.postValue("Failed to update medical history: " + e.getMessage()));
        } else {
            updateResultLiveData.postValue("User not authenticated");
        }
    }

    public void updateMedData(String caregiver, String maritalStatus, MutableLiveData<String> updateResultLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            DatabaseReference userRef = mDatabase.child("users").child(firebaseUser.getUid());
            Map<String, Object> updates = new HashMap<>();
            updates.put("caregiver", caregiver);
            updates.put("maritalStatus", maritalStatus);

            userRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> updateResultLiveData.postValue("Medical data updated successfully"))
                    .addOnFailureListener(e -> updateResultLiveData.postValue("Failed to update medical data: " + e.getMessage()));
        } else {
            updateResultLiveData.postValue("User not authenticated");
        }
    }

}


