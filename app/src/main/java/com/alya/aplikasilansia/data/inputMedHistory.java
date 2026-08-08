package com.alya.aplikasilansia.data;

public class inputMedHistory {
    public String penyakit;

    public void setPenyakit(String penyakit) {
        this.penyakit = penyakit;
    }

    public void setLamanya(String lamanya) {
        this.lamanya = lamanya;
    }

    public void setLamanyaBulan(String lamanyaBulan) {
        this.lamanyaBulan = lamanyaBulan;
    }

    public String lamanya;
    public String lamanyaBulan;

    public inputMedHistory() {
        // Default constructor required by Firebase
    }

    public inputMedHistory (String penyakit, String lamanya, String lamanyaBulan) {
        this.penyakit = penyakit;
        this.lamanya = lamanya;
        this.lamanyaBulan = lamanyaBulan;
    }

    public String getPenyakit() {
        return penyakit;
    }

    public String getLamanya() {
        return lamanya;
    }

    public String getLamanyaBulan() {
        return lamanyaBulan;
    }
}
