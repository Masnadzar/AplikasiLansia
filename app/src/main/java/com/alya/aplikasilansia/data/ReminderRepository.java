package com.alya.aplikasilansia.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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

    public interface OnReminderCreatedCallback {
        void onCreated(String reminderId);
    }

    // DIUBAH: tambah parameter callback OnReminderCreatedCallback supaya caller (AddReminderActivity)
    // bisa dapat reminderId yang di-generate Firestore -> dipakai sebagai requestCode unik
    // di ReminderScheduler. Sebelumnya scheduleReminder() dipanggil tanpa tahu ID sama sekali.
    public void createReminder(String title, String day, String time, String desc, String timestamp, Integer icon, MutableLiveData<FirebaseUser> reminderLiveData, MutableLiveData<String> errorLiveData, OnReminderCreatedCallback onCreatedCallback) {
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
                        Log.d("ReminderRepository", "Reminder added successfully, id=" + docRef.getId());
                        reminderLiveData.postValue(firebaseUser);
                        if (onCreatedCallback != null) {
                            onCreatedCallback.onCreated(docRef.getId()); // BARU: kirim balik ID dokumen
                        }
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

    // ================== BARU: Riwayat Pengingat ==================
    // Ditambahkan supaya setiap reminder yang SUDAH bunyi tercatat di subcollection
    // terpisah "reminder_history", tidak tercampur dengan reminder yang masih terjadwal
    // di subcollection "reminders". Dengan begini, riwayat tetap ada walau reminder
    // aslinya sudah lewat/terhapus.

    /**
     * Dipanggil dari ReminderReceiver setiap kali notifikasi reminder benar-benar muncul.
     * userId dikirim langsung dari ReminderScheduler (bukan dari FirebaseAuth.getCurrentUser())
     * karena BroadcastReceiver bisa saja dieksekusi sistem saat state auth belum tentu sama
     * dengan saat reminder pertama dijadwalkan.
     */
    public void addReminderHistory(String userId, String reminderId, String title, String desc, int icon, String triggeredAt) {
        if (userId == null) {
            Log.e("ReminderRepository", "addReminderHistory: userId is null, skip saving history");
            return;
        }
        ReminderHistory history = new ReminderHistory(reminderId, userId, title, desc, icon, triggeredAt);
        db.collection("users").document(userId).collection("reminder_history")
                .add(history)
                .addOnSuccessListener(docRef ->
                        Log.d("ReminderRepository", "Reminder history saved, id=" + docRef.getId()))
                .addOnFailureListener(e ->
                        Log.e("ReminderRepository", "Failed to save reminder history: " + e.getMessage()));
    }

    /**
     * Mengambil seluruh riwayat pengingat milik user yang sedang login,
     * diurutkan dari yang PALING BARU trigger ke yang paling lama.
     */
    public MutableLiveData<List<ReminderHistory>> fetchReminderHistory() {
        MutableLiveData<List<ReminderHistory>> historyLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid()).collection("reminder_history")
                    .orderBy("triggeredAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<ReminderHistory> histories = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            ReminderHistory history = doc.toObject(ReminderHistory.class);
                            history.setId(doc.getId());
                            histories.add(history);
                        }
                        historyLiveData.setValue(histories);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ReminderRepository", "Failed to fetch reminder history: " + e.getMessage());
                        historyLiveData.setValue(null);
                    });
        } else {
            historyLiveData.setValue(null);
        }

        return historyLiveData;
    }
}