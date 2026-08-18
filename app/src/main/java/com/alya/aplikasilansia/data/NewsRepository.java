package com.alya.aplikasilansia.data;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.alya.aplikasilansia.ui.news.News;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NewsRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public NewsRepository() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<List<News>> fetchAllNews() {
        MutableLiveData<List<News>> newsLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            mFirestore.collection("news")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<News> newsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String name = document.getString("name");
                            String date = document.getString("date");
                            String category = document.getString("category");
                            String source = document.getString("source");
                            String image = document.getString("image"); // nama field tetap "image", TIDAK di-rename
                            String newsContent = document.getString("newsContent");

                            Uri newsImageUri = (image != null) ? Uri.parse(image) : null;

                            News news = new News(name, date, category, source, newsImageUri, newsContent);
                            newsList.add(news);
                        }
                        newsLiveData.setValue(newsList);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NewsRepository", "Firestore error", e);
                        newsLiveData.setValue(null);
                    });
        }
        return newsLiveData;
    }
}