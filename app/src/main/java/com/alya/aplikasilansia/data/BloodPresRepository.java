package com.alya.aplikasilansia.data;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BloodPresRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // DIUBAH: dari DatabaseReference (RTDB) -> FirebaseFirestore
    private final MutableLiveData<List<BloodPressure>> pressureLiveData;

    public BloodPresRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // DIUBAH: FirebaseDatabase.getInstance().getReference()
        pressureLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<BloodPressure>> fetchingBloodPres() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // DIUBAH: DatabaseReference("users").child(uid).child("bloodPressure")
            //      -> subcollection: users/{uid}/bloodPressure
            db.collection("users").document(userId).collection("bloodPressure")
                    .get() // DIUBAH: addListenerForSingleValueEvent -> get() (sekali ambil)
                    .addOnSuccessListener(querySnapshot -> {
                        List<BloodPressure> bPressure = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date now = new Date();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String pres = doc.getString("pressure");
                            String pulse = doc.getString("pulse");
                            String date = doc.getString("date");

                            if (pres != null && pulse != null && date != null && !date.isEmpty()) {
                                BloodPressure pressure = new BloodPressure(pres, pulse, date);
                                bPressure.add(pressure);
                            }
                        }
                        // Sort the list by date
                        Collections.sort(bPressure, new Comparator<BloodPressure>() {
                            @Override
                            public int compare(BloodPressure o1, BloodPressure o2) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                try {
                                    Date date1 = dateFormat.parse(o1.getBpDate());
                                    Date date2 = dateFormat.parse(o2.getBpDate());
                                    if (date1 != null && date2 != null) {
                                        return date2.compareTo(date1); // Latest first
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return 0;
                            }
                        });
                        bPressure.removeIf(bpressure -> {
                            try {
                                Date pressureDate = sdf.parse(bpressure.getBpDate());
                                return pressureDate.before(now);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return false;
                            }
                        });
                        pressureLiveData.setValue(bPressure);
                    })
                    .addOnFailureListener(e -> {
                        // DIUBAH: onCancelled -> addOnFailureListener
                        Log.e("BloodPresRepository", "Firestore error: ", e);
                        pressureLiveData.setValue(null);
                    });
        } else {
            pressureLiveData.setValue(null);
        }
        return pressureLiveData;
    }

    // Method to get the latest BloodPressure data
    public LiveData<BloodPressure> getLatestBloodPressure() {
        MutableLiveData<BloodPressure> latestBloodPressureLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            latestBloodPressureLiveData.setValue(null); // User not authenticated
            return latestBloodPressureLiveData;
        } else {
            String userId = firebaseUser.getUid();

            // DIUBAH: addValueEventListener (realtime RTDB) -> addSnapshotListener (realtime Firestore)
            db.collection("users").document(userId).collection("bloodPressure")
                    .addSnapshotListener((querySnapshot, error) -> {
                        if (error != null) {
                            // DIUBAH: onCancelled -> callback error di addSnapshotListener
                            Log.e("BloodPresRepository", "Database error", error);
                            return;
                        }
                        if (querySnapshot == null) return;

                        List<BloodPressure> bloodPressureList = new ArrayList<>();
                        for (QueryDocumentSnapshot ds : querySnapshot) {
                            String pres = ds.getString("pressure");
                            String pulse = ds.getString("pulse");
                            String date = ds.getString("date");

                            if (pres != null && pulse != null && date != null && !date.isEmpty()) {
                                BloodPressure pressure = new BloodPressure(pres, pulse, date);
                                bloodPressureList.add(pressure);
                            }
                        }
                        // Sort the list by date in descending order
                        bloodPressureList.sort((bp1, bp2) -> {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                            String date1Str = bp1.getBpDate();
                            String date2Str = bp2.getBpDate();

                            if (date1Str == null || date2Str == null) {
                                Log.e("BloodPresRepository", "One or both dates are null. Date1: " + date1Str + ", Date2: " + date2Str);
                                return 0;
                            } else {
                                try {
                                    Date date1 = dateFormat.parse(date1Str);
                                    Date date2 = dateFormat.parse(date2Str);

                                    if (date1 != null && date2 != null) {
                                        return date2.compareTo(date1); // Latest first
                                    }
                                } catch (ParseException e) {
                                    Log.e("BloodPresRepository", "Date parse error", e);
                                }
                                return 0;
                            }
                        });
                        // Set the most recent BloodPressure as the value
                        if (!bloodPressureList.isEmpty()) {
                            latestBloodPressureLiveData.setValue(bloodPressureList.get(0));
                        } else {
                            latestBloodPressureLiveData.setValue(null);
                        }
                    });
        }
        return latestBloodPressureLiveData;
    }

    public LiveData<List<BloodPressure>> getBloodPressureLiveData() {
        return pressureLiveData;
    }

    public void addPressure(String bloodPressure, String pulse, String timestamp, MutableLiveData<FirebaseUser> pressureLiveData, MutableLiveData<String> errorLiveData) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            Log.d("BloodPresRepository", "User ID: " + userId);

            Map<String, Object> data = new HashMap<>();
            data.put("pressure", bloodPressure);
            data.put("pulse", pulse);
            data.put("date", timestamp);

            // DIUBAH: DatabaseReference.push().setValue(data) -> collection.add(data)
            // .add() otomatis generate document ID baru, setara dengan .push() di RTDB
            db.collection("users").document(userId).collection("bloodPressure")
                    .add(data)
                    .addOnSuccessListener(docRef -> {
                        Log.d("BloodPresRepository", "Blood Pressure added successfully");
                        pressureLiveData.postValue(firebaseUser);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BloodPresRepository", "Failed to add Blood Pressure: " + e.getMessage());
                        errorLiveData.postValue("Failed to add Blood Pressure: " + e.getMessage());
                    });
        } else {
            errorLiveData.postValue("User not authenticated");
        }
    }
}