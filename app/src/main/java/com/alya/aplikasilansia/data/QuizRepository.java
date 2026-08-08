package com.alya.aplikasilansia.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

public class QuizRepository {

    private final DatabaseReference databaseReference;
    private final MutableLiveData<List<Question>> questionsLiveData;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<List<QuizHistoryItem>> quizHistoryLiveData;


    public QuizRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("questions");
        questionsLiveData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        quizHistoryLiveData = new MutableLiveData<>();
        fetchQuestions();
    }

    private void fetchQuestions() {
        isLoading.setValue(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Question> questions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Question question = snapshot.getValue(Question.class);
                    questions.add(question);
                }
                questionsLiveData.setValue(questions);
                isLoading.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                isLoading.setValue(false);
            }
        });
    }
    public void fetchQuizHistory(String userId) {
        DatabaseReference userQuizzesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("quizzes");
        userQuizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<QuizHistoryItem> quizHistoryItems = new ArrayList<>();
                for (DataSnapshot quizSnapshot : snapshot.getChildren()) {
                    String classifiedScore = quizSnapshot.child("classification").getValue(String.class);
                    String date = quizSnapshot.child("dateQuiz").getValue(String.class); // Date as String
                    int totalScore = quizSnapshot.child("score").getValue(Integer.class);
                    quizHistoryItems.add(new QuizHistoryItem(classifiedScore,totalScore, date));
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("QuizRepository", "Database error: ", error.toException());
            }
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

    public void storeAnswers(String userId, String quizId, Map<String, Boolean> userAnswers, int score, String classification, String date,  OnStoreAnswersCompleteListener listener) {
        DatabaseReference answersRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("quizzes").child(quizId);

        Map<String, Object> data = new HashMap<>();
        data.put("answers", userAnswers);
        data.put("score", score);
        data.put("classification", classification);
        data.put("dateQuiz", date); // Use current time in milliseconds

        Log.d("QuizRepository", "Storing answers for quizId: " + quizId + ", userId: " + userId + ", answers: " + userAnswers.toString() + ", score: " + score);

        answersRef.setValue(data).addOnCompleteListener(task -> {
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