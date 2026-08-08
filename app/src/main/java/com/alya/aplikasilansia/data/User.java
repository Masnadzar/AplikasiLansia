package com.alya.aplikasilansia.data;

import android.net.Uri;

import java.util.List;

public class User {
    private String email;
    private String birthDate;
    private String userName;
    private Uri profileImageUrl;
    private String caregiver;
    private String maritalStatus;

    private String gender;
    private List<inputMedHistory> medHistory;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public User(String email, String birthDate, String userName, String gender, Uri profileImageUrl, String caregiver, String maritalStatus, List<inputMedHistory> medHistory) {
        this.email = email;
        this.birthDate = birthDate;
        this.userName = userName;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        this.caregiver = caregiver;
        this.maritalStatus = maritalStatus;
        this.medHistory = medHistory;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getUserName(){
        return userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }
    public Uri getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(Uri profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public String getCaregiver() {
        return caregiver;
    }

    public void setCaregiver(String caregiver) {
        this.caregiver = caregiver;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public List<inputMedHistory> getMedHistory() {
        return medHistory;
    }

    public void setMedHistory(List<inputMedHistory> medHistory) {
        this.medHistory = medHistory;
    }
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}
