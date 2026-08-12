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
    private FirebaseFirestore db; // DIUBAH: dari DatabaseReference (RTDB) -> FirebaseFirestore

    public HealthCareRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // DIUBAH: FirebaseDatabase.getInstance().getReference()
    }

    public MutableLiveData<List<HealthCare>> fetchHealthCare() {
        MutableLiveData<List<HealthCare>> healthCareLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            // DIUBAH: mDatabase.child("health_center") -> collection("health_center")
            // (top-level collection, sama seperti sebelumnya top-level node di RTDB)
            db.collection("health_center")
                    .get() // DIUBAH: addListenerForSingleValueEvent -> get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<HealthCare> healthCareList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String name = doc.getString("name");
                            String city = doc.getString("city");
                            String address = doc.getString("address");
                            String url = doc.getString("url");

                            HealthCare healthCare = new HealthCare(name, address, city, url);
                            healthCareList.add(healthCare);
                        }
                        healthCareLiveData.setValue(healthCareList);
                    })
                    .addOnFailureListener(e -> {
                        // DIUBAH: onCancelled -> addOnFailureListener
                        Log.e("HealthCareRepository", "Firestore error", e);
                        healthCareLiveData.setValue(null);
                    });
        }
        return healthCareLiveData;
    }
}