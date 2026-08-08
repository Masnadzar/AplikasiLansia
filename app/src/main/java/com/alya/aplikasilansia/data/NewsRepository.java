package com.alya.aplikasilansia.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.alya.aplikasilansia.ui.news.News;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class NewsRepository {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage; // Firebase Storage reference

    public NewsRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference("news_images"); // Storage reference
    }

    public MutableLiveData<List<News>> fetchAllNews() {
        MutableLiveData<List<News>> newsLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            DatabaseReference newsRef = mDatabase.child("news");

            newsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<News> newsList = new ArrayList<>();
                    for (DataSnapshot newsSnapshot : snapshot.getChildren()) {
                        String name = newsSnapshot.child("name").getValue(String.class);
                        String date = newsSnapshot.child("date").getValue(String.class);
                        String category = newsSnapshot.child("category").getValue(String.class);
                        String source = newsSnapshot.child("source").getValue(String.class);
                        String image = newsSnapshot.child("image").getValue(String.class);
                        String newsContent = newsSnapshot.child("newsContent").getValue(String.class);

                        Uri newsImageUri = (image != null) ? Uri.parse(image) : null;

                        News news = new News(name, date, category, source, newsImageUri, newsContent);
                        newsList.add(news);
                    }
                    newsLiveData.setValue(newsList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("NewsRepository", "Database error: ", error.toException());
                    newsLiveData.setValue(null); // Optionally set the value to null to indicate a failure
                }
            });
        }
        return newsLiveData;
    }
}