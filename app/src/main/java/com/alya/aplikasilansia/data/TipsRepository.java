package com.alya.aplikasilansia.data;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.alya.aplikasilansia.ui.berita.Tips;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TipsRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public TipsRepository() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<List<Tips>> fetchAllTips() {
        MutableLiveData<List<Tips>> tipsLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            mFirestore.collection("tips")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Tips> tipsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String title = document.getString("title");
                            String date = document.getString("date");
                            String category = document.getString("category");
                            String content = document.getString("content");
                            String image = document.getString("image");

                            Uri tipsImageUri = (image != null && !image.isEmpty()) ? Uri.parse(image) : null;

                            Tips tips = new Tips(title, date, category, content, tipsImageUri);
                            tipsList.add(tips);
                        }
                        tipsLiveData.setValue(tipsList);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TipsRepository", "Firestore error", e);
                        tipsLiveData.setValue(null);
                    });
        } else {
            tipsLiveData.setValue(new ArrayList<>());
        }
        return tipsLiveData;
    }
}