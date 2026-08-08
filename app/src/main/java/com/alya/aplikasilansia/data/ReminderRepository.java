package com.alya.aplikasilansia.data;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReminderRepository {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    public ReminderRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference("reminders");
    }

    public MutableLiveData<List<Reminder>> fetchReminder() {
        MutableLiveData<List<Reminder>> reminderLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            DatabaseReference userRef = mDatabase.child("reminders").child(firebaseUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Reminder> reminders = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date now = new Date();

                    for (DataSnapshot reminderSnapshot : snapshot.getChildren()) {
                        String id = reminderSnapshot.getKey(); // Get the ID from the snapshot key
                        String userId = reminderSnapshot.child("userId").getValue(String.class);
                        if (userId.equals(firebaseUser.getUid())) {
                            String title = reminderSnapshot.child("title").getValue(String.class);
                            String day = reminderSnapshot.child("day").getValue(String.class);
                            String time = reminderSnapshot.child("time").getValue(String.class);
                            String desc = reminderSnapshot.child("desc").getValue(String.class);
                            String timestamp = reminderSnapshot.child("timestamp").getValue(String.class);
                            Integer icon = reminderSnapshot.child("icon").getValue(Integer.class);

                            Reminder reminder = new Reminder(userId, id, title, day, time, desc, timestamp, icon);
                            reminders.add(reminder);
                        }
                    }
                    reminders.removeIf(reminder -> {
                        try {
                            Date reminderDate = sdf.parse(reminder.getTimestamp());
                            return reminderDate.before(now);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                    });
                    reminderLiveData.setValue(reminders); // Set LiveData value here
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ReminderRepository", "Database error: ", error.toException());
                    reminderLiveData.setValue(null);
                }
            });
        }

        return reminderLiveData;
    }



    public void createReminder(String title, String day, String time, String desc, String timestamp, Integer icon, MutableLiveData<FirebaseUser> reminderLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            Log.d(TAG, "User ID: " + userId); // Log the user ID to verify it's correct
            Reminder reminder = new Reminder(userId, null ,title, day, time, desc, timestamp, icon);
            DatabaseReference remindersRef = mDatabase.child("reminders").child(userId).push();
            remindersRef.setValue(reminder)
                    .addOnSuccessListener(aVoid -> {
                        // Handle success
                        Log.d(TAG, "Reminder added successfully");
                        reminderLiveData.postValue(firebaseUser); // Update the live data with the user
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Log.e(TAG, "Failed to add reminder: " + e.getMessage());
                        errorLiveData.postValue("Failed to add reminder: " + e.getMessage());
                    });
        } else {
            errorLiveData.postValue("User not authenticated");
        }
    }
    public void editReminder(String reminderId, String title, String day, String time, String desc, String timestamp, Integer icon, MutableLiveData<String> errorLiveData, Runnable onSuccess) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference reminderRef = mDatabase.child("reminders").child(userId).child(reminderId);

            // Create a map of updated values
            Map<String, Object> updatedValues = new HashMap<>();
            updatedValues.put("title", title);
            updatedValues.put("day", day);
            updatedValues.put("time", time);
            updatedValues.put("desc", desc);
            updatedValues.put("timestamp", timestamp);
            updatedValues.put("icon", icon);

            reminderRef.updateChildren(updatedValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Reminder updated successfully");
                        if (onSuccess != null) {
                            onSuccess.run(); // Notify that the update was successful
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update reminder: " + e.getMessage());
                        errorLiveData.postValue("Failed to update reminder: " + e.getMessage());
                    });
        } else {
            errorLiveData.postValue("User not authenticated");
        }
    }

    public interface OnReminderDeletedCallback {
        void onReminderDeleted();
    }
    public void deleteReminder(String reminderId, MutableLiveData<String> errorLiveData, OnReminderDeletedCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference reminderRef = mDatabase.child("reminders").child(userId).child(reminderId);

            reminderRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Reminder deleted successfully");
//                        fetchReminder();
                        callback.onReminderDeleted(); // Trigger the callback on successful deletion
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete reminder: " + e.getMessage());
                        errorLiveData.postValue("Failed to delete reminder: " + e.getMessage());
                    });
        } else {
            errorLiveData.postValue("User not authenticated");
        }
    }


}
