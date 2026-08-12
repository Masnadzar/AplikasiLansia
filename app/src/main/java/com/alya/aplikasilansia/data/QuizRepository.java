package com.alya.aplikasilansia.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

public class QuizRepository {

    private final FirebaseFirestore db; // DIUBAH: dari DatabaseReference (RTDB) -> FirebaseFirestore
    private final MutableLiveData<List<Question>> questionsLiveData;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<List<QuizHistoryItem>> quizHistoryLiveData;

    public QuizRepository() {
        db = FirebaseFirestore.getInstance(); // DIUBAH: FirebaseDatabase.getInstance().getReference("questions")
        questionsLiveData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        quizHistoryLiveData = new MutableLiveData<>();
        fetchQuestions();
    }

    private void fetchQuestions() {
        isLoading.setValue(true);

        // DIUBAH: addValueEventListener (realtime RTDB) -> addSnapshotListener (realtime Firestore)
        db.collection("questions")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        // DIUBAH: onCancelled -> callback error di addSnapshotListener
                        Log.e("QuizRepository", "Firestore error", error);
                        isLoading.setValue(false);
                        return;
                    }
                    if (querySnapshot == null) return;

                    List<Question> questions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // DIUBAH: snapshot.getValue(Question.class) -> doc.toObject(Question.class)
                        Question question = doc.toObject(Question.class);
                        questions.add(question);
                    }
                    questionsLiveData.setValue(questions);
                    isLoading.setValue(false);
                });
    }

    public void fetchQuizHistory(String userId) {
        // DIUBAH: mDatabase.child("users").child(uid).child("quizzes") -> subcollection users/{uid}/quizzes
        db.collection("users").document(userId).collection("quizzes")
                .get() // DIUBAH: addListenerForSingleValueEvent -> get()
                .addOnSuccessListener(querySnapshot -> {
                    List<QuizHistoryItem> quizHistoryItems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String classifiedScore = doc.getString("classification");
                        String date = doc.getString("dateQuiz"); // Date as String
                        Long totalScore = doc.getLong("score"); // DIUBAH: getValue(Integer.class) -> getLong (Firestore number = Long)
                        quizHistoryItems.add(new QuizHistoryItem(
                                classifiedScore,
                                totalScore != null ? totalScore.intValue() : 0,
                                date
                        ));
                    }
                    // Sort the list by date
                    Collections.sort(quizHistoryItems, new Comparator<QuizHistoryItem>() {
                        @Override
                        public int compare(QuizHistoryItem o1, QuizHistoryItem o2) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy  HH:mm", new Locale("id"));
                            try {
                                Date date1 = dateFormat.parse(o1.getDate());
                                Date date2 = dateFormat.parse(o2.getDate());
                                if (date1 != null && date2 != null) {
                                    return date2.compareTo(date1); // Latest first
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });
                    quizHistoryLiveData.setValue(quizHistoryItems);
                })
                .addOnFailureListener(e -> {
                    // DIUBAH: onCancelled -> addOnFailureListener
                    Log.e("QuizRepository", "Firestore error: ", e);
                });
    }

    public LiveData<List<QuizHistoryItem>> getQuizHistoryLiveData() {
        return quizHistoryLiveData;
    }

    public LiveData<List<Question>> getQuestionsLiveData() {
        return questionsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void storeAnswers(String userId, String quizId, Map<String, Boolean> userAnswers, int score, String classification, String date, OnStoreAnswersCompleteListener listener) {
        // DIUBAH: DatabaseReference("users").child(uid).child("quizzes").child(quizId)
        //      -> document di subcollection users/{uid}/quizzes/{quizId}
        Map<String, Object> data = new HashMap<>();
        data.put("answers", userAnswers);
        data.put("score", score);
        data.put("classification", classification);
        data.put("dateQuiz", date);

        Log.d("QuizRepository", "Storing answers for quizId: " + quizId + ", userId: " + userId + ", answers: " + userAnswers.toString() + ", score: " + score);

        // DIUBAH: remindersRef.setValue(data) -> documentRef.set(data)
        db.collection("users").document(userId).collection("quizzes").document(quizId)
                .set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure("Failed to submit answers. Please try again.");
                    }
                });
    }

    public interface OnStoreAnswersCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}