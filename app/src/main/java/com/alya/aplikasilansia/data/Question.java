package com.alya.aplikasilansia.data;

public class Question {

    private String text;
    private boolean correctAnswer;

    public Question() {
        // Default constructor required for calls to DataSnapshot.getValue(Question.class)
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
}





