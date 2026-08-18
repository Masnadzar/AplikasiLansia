package com.alya.aplikasilansia.data;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
    private FirebaseFirestore db;
    private final MutableLiveData<List<BloodPressure>> pressureLiveData;

    public BloodPresRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        pressureLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<BloodPressure>> fetchingBloodPres() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            db.collection("users").document(userId).collection("bloodPressure")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuerySnapshot snapshot = task.getResult();
                            List<BloodPressure> bPressure = new ArrayList<>();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            Date now = new Date();

                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                String pres = doc.getString("pressure");
                                String pulse = doc.getString("pulse");
                                String date = doc.getString("date");

                                if (pres != null && pulse != null && date != null && !date.isEmpty()) {
                                    BloodPressure pressure = new BloodPressure(pres, pulse, date);
                                    bPressure.add(pressure);
                                }
                            }
                            Collections.sort(bPressure, new Comparator<BloodPressure>() {
                                @Override
                                public int compare(BloodPressure o1, BloodPressure o2) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    try {
                                        Date date1 = dateFormat.parse(o1.getBpDate());
                                        Date date2 = dateFormat.parse(o2.getBpDate());
                                        if (date1 != null && date2 != null) {
                                            return date2.compareTo(date1);
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
                        } else {
                            Log.e("BloodPresRepository", "Firestore error: ", task.getException());
                            pressureLiveData.setValue(null);
                        }
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
            latestBloodPressureLiveData.setValue(null);
            return latestBloodPressureLiveData;
        } else {
            String userId = firebaseUser.getUid();
            db.collection("users").document(userId).collection("bloodPressure")
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null) {
                            Log.e("BloodPresRepository", "Firestore error", error);
                            return;
                        }
                        if (snapshot == null) return;

                        List<BloodPressure> bloodPressureList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            String pres = doc.getString("pressure");
                            String pulse = doc.getString("pulse");
                            String date = doc.getString("date");

                            if (pres != null && pulse != null && date != null && !date.isEmpty()) {
                                BloodPressure pressure = new BloodPressure(pres, pulse, date);
                                bloodPressureList.add(pressure);
                            }
                        }
                        bloodPressureList.sort((bp1, bp2) -> {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            String date1Str = bp1.getBpDate();
                            String date2Str = bp2.getBpDate();

                            if (date1Str == null || date2Str == null) {
                                return 0;
                            } else {
                                try {
                                    Date date1 = dateFormat.parse(date1Str);
                                    Date date2 = dateFormat.parse(date2Str);
                                    if (date1 != null && date2 != null) {
                                        return date2.compareTo(date1);
                                    }
                                } catch (ParseException e) {
                                    Log.e("BloodPresRepository", "Date parse error", e);
                                }
                                return 0;
                            }
                        });

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
            Log.d(TAG, "User ID: " + userId);

            Map<String, Object> data = new HashMap<>();
            data.put("pressure", bloodPressure);
            data.put("pulse", pulse);
            data.put("date", timestamp);

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