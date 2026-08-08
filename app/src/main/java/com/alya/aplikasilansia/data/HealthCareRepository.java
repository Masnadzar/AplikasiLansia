package com.alya.aplikasilansia.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.alya.aplikasilansia.ui.healthcare.HealthCare;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HealthCareRepository {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public HealthCareRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public MutableLiveData<List<HealthCare>> fetchHealthCare() {
        MutableLiveData<List<HealthCare>> healthCareLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            DatabaseReference healthcareRef = mDatabase.child("health_center");

            healthcareRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        List<HealthCare> healthCareList = new ArrayList<>();
                        for (DataSnapshot newSnapshot : snapshot.getChildren()) {
                            String name = newSnapshot.child("name").getValue(String.class);
                            String city = newSnapshot.child("city").getValue(String.class);
                            String address = newSnapshot.child("address").getValue(String.class);
                            String url = newSnapshot.child("url").getValue(String.class);

                            HealthCare healthCare = new HealthCare(name, address, city, url);
                            healthCareList.add(healthCare);
                        }
                        healthCareLiveData.setValue(healthCareList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HealthCareRepository","Database error", error.toException());
                    healthCareLiveData.setValue(null);
                }
            });
        }
        return healthCareLiveData;
    }
}
