package com.alya.aplikasilansia.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizRepository {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<Question>> questionsLiveData;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<List<QuizHistoryItem>> quizHistoryLiveData;

    public QuizRepository() {
        db = FirebaseFirestore.getInstance();
        questionsLiveData = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        quizHistoryLiveData = new MutableLiveData<>();
        fetchQuestions();
    }

    private void fetchQuestions() {
        isLoading.setValue(true);
        db.collection("quiz_questions")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("QuizRepository", "Error fetching questions: ", error);
                        isLoading.setValue(false);
                        return;
                    }
                    if (querySnapshot == null) {
                        isLoading.setValue(false);
                        return;
                    }

                    List<Question> questions = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Question question = doc.toObject(Question.class);
                        if (question != null) {
                            question.setDocId(doc.getId());
                            questions.add(question);
                        }
                    }

                    // ID dokumen "question1".."question15" - sort berdasarkan angka
                    // supaya soal tetap urut 1-15, bukan urut string dari Firestore
                    Collections.sort(questions, new Comparator<Question>() {
                        @Override
                        public int compare(Question q1, Question q2) {
                            return Integer.compare(extractNumber(q1.getDocId()), extractNumber(q2.getDocId()));
                        }
                    });

                    questionsLiveData.setValue(questions);
                    isLoading.setValue(false);
                });
    }

    private int extractNumber(String docId) {
        if (docId == null) return Integer.MAX_VALUE;
        Matcher matcher = Pattern.compile("\\d+").matcher(docId);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return Integer.MAX_VALUE;
    }

    public void fetchQuizHistory(String userId) {
        db.collection("users").document(userId).collection("quizResults")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot snapshot = task.getResult();
                        List<QuizHistoryItem> quizHistoryItems = new ArrayList<>();
                        for (DocumentSnapshot quizDoc : snapshot.getDocuments()) {
                            String classifiedScore = quizDoc.getString("classification");
                            String date = quizDoc.getString("dateQuiz");
                            Long totalScoreLong = quizDoc.getLong("score");
                            int totalScore = totalScoreLong != null ? totalScoreLong.intValue() : 0;
                            quizHistoryItems.add(new QuizHistoryItem(classifiedScore, totalScore, date));
                        }
                        // Sort by date, terbaru duluan - sama seperti sebelumnya
                        Collections.sort(quizHistoryItems, new Comparator<QuizHistoryItem>() {
                            @Override
                            public int compare(QuizHistoryItem o1, QuizHistoryItem o2) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy  HH:mm", new Locale("id"));
                                try {
                                    Date date1 = dateFormat.parse(o1.getDate());
                                    Date date2 = dateFormat.parse(o2.getDate());
                                    if (date1 != null && date2 != null) {
                                        return date2.compareTo(date1);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return 0;
                            }
                        });
                        quizHistoryLiveData.setValue(quizHistoryItems);
                    } else {
                        Log.e("QuizRepository", "Firestore error: ", task.getException());
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

    public void storeAnswers(String userId, String quizId, Map<String, Boolean> userAnswers, int score, String classification, String date, OnStoreAnswersCompleteListener listener) {
        CollectionReference quizResultsRef = db.collection("users").document(userId).collection("quizResults");

        Map<String, Object> data = new HashMap<>();
        data.put("answers", userAnswers);
        data.put("score", score);
        data.put("classification", classification);
        data.put("dateQuiz", date);

        Log.d("QuizRepository", "Storing answers for userId: " + userId + ", answers: " + userAnswers.toString() + ", score: " + score);

        // quizId tidak dipakai sebagai document ID lagi - skema quizResults pakai autoId
        quizResultsRef.add(data).addOnCompleteListener(task -> {
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