package com.alya.aplikasilansia.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private FirebaseFirestore db; // DIUBAH: dari DatabaseReference (RTDB) -> FirebaseFirestore

    public ReminderRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // DIUBAH: FirebaseDatabase.getInstance().getReference()
    }

    public MutableLiveData<List<Reminder>> fetchReminder() {
        MutableLiveData<List<Reminder>> reminderLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            // DIUBAH: mDatabase.child("reminders").child(uid) -> subcollection users/{uid}/reminders
            db.collection("users").document(firebaseUser.getUid()).collection("reminders")
                    .get() // DIUBAH: addListenerForSingleValueEvent -> get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Reminder> reminders = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date now = new Date();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String id = doc.getId(); // DIUBAH: reminderSnapshot.getKey() -> doc.getId()
                            String userId = doc.getString("userId");
                            if (userId != null && userId.equals(firebaseUser.getUid())) {
                                String title = doc.getString("title");
                                String day = doc.getString("day");
                                String time = doc.getString("time");
                                String desc = doc.getString("desc");
                                String timestamp = doc.getString("timestamp");
                                Long iconLong = doc.getLong("icon"); // DIUBAH: getValue(Integer.class) -> getLong

                                Reminder reminder = new Reminder(userId, id, title, day, time, desc, timestamp,
                                        iconLong != null ? iconLong.intValue() : 0);
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
                        reminderLiveData.setValue(reminders);
                    })
                    .addOnFailureListener(e -> {
                        // DIUBAH: onCancelled -> addOnFailureListener
                        Log.e("ReminderRepository", "Firestore error: ", e);
                        reminderLiveData.setValue(null);
                    });
        }

        return reminderLiveData;
    }

    public void createReminder(String title, String day, String time, String desc, String timestamp, Integer icon, MutableLiveData<FirebaseUser> reminderLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            Log.d("ReminderRepository", "User ID: " + userId);
            Reminder reminder = new Reminder(userId, null, title, day, time, desc, timestamp, icon);

            // DIUBAH: mDatabase.child("reminders").child(uid).push().setValue(reminder)
            //      -> collection("users").document(uid).collection("reminders").add(reminder)
            // .add() otomatis generate document ID baru, setara dengan .push() di RTDB
            db.collection("users").document(userId).collection("reminders")
                    .add(reminder)
                    .addOnSuccessListener(docRef -> {
                        Log.d("ReminderRepository", "Reminder added successfully");
                        reminderLiveData.postValue(firebaseUser);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ReminderRepository", "Failed to add reminder: " + e.getMessage());
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

            // DIUBAH: DatabaseReference reminderRef -> DocumentReference reminderRef
            Map<String, Object> updatedValues = new HashMap<>();
            updatedValues.put("title", title);
            updatedValues.put("day", day);
            updatedValues.put("time", time);
            updatedValues.put("desc", desc);
            updatedValues.put("timestamp", timestamp);
            updatedValues.put("icon", icon);

            // DIUBAH: reminderRef.updateChildren(updatedValues) -> reminderRef.update(updatedValues)
            db.collection("users").document(userId).collection("reminders").document(reminderId)
                    .update(updatedValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ReminderRepository", "Reminder updated successfully");
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ReminderRepository", "Failed to update reminder: " + e.getMessage());
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

            // DIUBAH: reminderRef.removeValue() -> reminderRef.delete()
            db.collection("users").document(userId).collection("reminders").document(reminderId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ReminderRepository", "Reminder deleted successfully");
                        callback.onReminderDeleted();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ReminderRepository", "Failed to delete reminder: " + e.getMessage());
                        errorLiveData.postValue("Failed to delete reminder: " + e.getMessage());
                    });
        } else {
            errorLiveData.postValue("User not authenticated");
        }
    }
}