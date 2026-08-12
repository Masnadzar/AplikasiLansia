package com.alya.aplikasilansia.data;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.alya.aplikasilansia.ui.news.News;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class NewsRepository {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // DIUBAH: dari DatabaseReference (RTDB) -> FirebaseFirestore
    private StorageReference mStorage; // Firebase Storage reference -> TIDAK BERUBAH

    public NewsRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // DIUBAH: FirebaseDatabase.getInstance().getReference()
        mStorage = FirebaseStorage.getInstance().getReference("news_images"); // Storage reference
    }

    public MutableLiveData<List<News>> fetchAllNews() {
        MutableLiveData<List<News>> newsLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            // DIUBAH: mDatabase.child("news") -> collection("news")
            db.collection("news")
                    .get() // DIUBAH: addListenerForSingleValueEvent -> get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<News> newsList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String name = doc.getString("name");
                            String date = doc.getString("date");
                            String category = doc.getString("category");
                            String source = doc.getString("source");
                            String image = doc.getString("image");
                            String newsContent = doc.getString("newsContent");

                            Uri newsImageUri = (image != null) ? Uri.parse(image) : null;

                            News news = new News(name, date, category, source, newsImageUri, newsContent);
                            newsList.add(news);
                        }
                        newsLiveData.setValue(newsList);
                    })
                    .addOnFailureListener(e -> {
                        // DIUBAH: onCancelled -> addOnFailureListener
                        Log.e("NewsRepository", "Firestore error: ", e);
                        newsLiveData.setValue(null);
                    });
        }
        return newsLiveData;
    }
}