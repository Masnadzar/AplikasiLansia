package com.alya.aplikasilansia.data;

public class BloodPressure {
    private String bloodPressure;
    private String pulse;
    private String bpDate;
    public BloodPressure () {
        // Default constructor
    }

    public BloodPressure (String bloodPressure, String pulse, String bpDate) {
        this.bloodPressure = bloodPressure;
        this.pulse = pulse;
        this.bpDate = bpDate;
    }
    public String getBloodPressure() {
        return bloodPressure;
    }

    public String getPulse() {
        return pulse;
    }

    public String getBpDate() {
        return bpDate;
    }
}
