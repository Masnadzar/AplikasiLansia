package com.alya.aplikasilansia.data;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.alya.aplikasilansia.ui.healthcare.HealthCare;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HealthCareRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public HealthCareRepository() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<List<HealthCare>> fetchHealthCare() {
        MutableLiveData<List<HealthCare>> healthCareLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            mFirestore.collection("health_centers")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<HealthCare> healthCareList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String name = document.getString("name");
                            String city = document.getString("city");
                            String address = document.getString("address");
                            String url = document.getString("url");

                            HealthCare healthCare = new HealthCare(name, address, city, url);
                            healthCareList.add(healthCare);
                        }
                        healthCareLiveData.setValue(healthCareList);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HealthCareRepository", "Firestore error", e);
                        healthCareLiveData.setValue(null);
                    });
        }
        return healthCareLiveData;
    }
}