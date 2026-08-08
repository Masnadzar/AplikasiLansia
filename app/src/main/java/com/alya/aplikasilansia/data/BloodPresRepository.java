package com.alya.aplikasilansia.data;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private DatabaseReference mDatabase;
    private final MutableLiveData<List<BloodPressure>> pressureLiveData;

    public BloodPresRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        pressureLiveData = new MutableLiveData<>();
    }
    public MutableLiveData<List<BloodPressure>> fetchingBloodPres() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference pressureRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("bloodPressure");

            pressureRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<BloodPressure> bPressure = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date now = new Date();

                    for (DataSnapshot pressureSnapshot : snapshot.getChildren()) {
                        String pres = pressureSnapshot.child("pressure").getValue(String.class);
                        String pulse = pressureSnapshot.child("pulse").getValue(String.class);
                        String date = pressureSnapshot.child("date").getValue(String.class);

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
                    pressureLiveData.setValue(bPressure); // Set LiveData value here
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ReminderRepository", "Database error: ", error.toException());
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
            latestBloodPressureLiveData.setValue(null); // User not authenticated
            return latestBloodPressureLiveData;
        } else {

            String userId = firebaseUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("bloodPressure");

            databaseReference.orderByChild("date") // Replace "timestamp" with your date field
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<BloodPressure> bloodPressureList = new ArrayList<>();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String pres = ds.child("pressure").getValue(String.class);
                                String pulse = ds.child("pulse").getValue(String.class);
                                String date = ds.child("date").getValue(String.class);

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
                                    return 0; // No comparison if dates are null
                                } else {
                                    Log.e("BloodPresRepository", "One or both dates are null. Date1: " + date1Str + ", Date2: " + date2Str);
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
                                latestBloodPressureLiveData.setValue(null); // No data available
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("BloodPresRepository", "Database error", error.toException());
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
            Log.d(TAG, "User ID: " + userId); // Log the user ID to verify it's correct
            DatabaseReference pressureRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("bloodPressure").push();

            Map<String, Object> data = new HashMap<>();
            data.put("pressure", bloodPressure);
            data.put("pulse", pulse);
            data.put("date", timestamp);
            pressureRef.setValue(data)
                    .addOnSuccessListener(aVoid -> {
                        // Handle success
                        Log.d("BloodPresRepository", "Blood Pressure added successfully");
                        pressureLiveData.postValue(firebaseUser); // Update the live data with the user
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Log.e("BloodPresRepository", "Failed to add Blood Pressure: " + e.getMessage());
                        errorLiveData.postValue("Failed to add Blood Pressure: " + e.getMessage());
                    });
        } else {
            errorLiveData.postValue("User not authenticated");
        }
    }
}
