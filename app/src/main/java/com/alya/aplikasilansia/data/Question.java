package com.alya.aplikasilansia.data;

public class Question {

    private String text;
    private boolean correctAnswer;
    private int score;

    // Bukan field Firestore - dipakai lokal untuk urutan soal 1-15 (lihat QuizRepository)
    private String docId;

    public Question() {
        // Default constructor required for calls to DocumentSnapshot.toObject(Question.class)
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}