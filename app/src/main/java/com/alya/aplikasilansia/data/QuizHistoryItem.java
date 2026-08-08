package com.alya.aplikasilansia.data;

public class QuizHistoryItem {
    private String classifiedScore;
    private String date;
    private int totalScore;

    public QuizHistoryItem(String classifiedScore, int totalScore, String date) {
        this.classifiedScore = classifiedScore;
        this.date = date;
        this.totalScore = totalScore;
    }
    public String getClassifiedScore() {
        return classifiedScore;
    }
    public String getDate() {
        return date;
    }
    public int getTotalScore() {
        return totalScore;
    }
}

