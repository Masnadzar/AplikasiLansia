package com.alya.aplikasilansia.data;

import android.net.Uri;
import java.util.List;

public class UserData {
    private static UserData instance;
    private String email;
    private String password; // Added password field
    private String birthDate;
    private String userName;
    private String gender;
    private String caregiver;
    private String maritalStatus;
    private Uri profileImageUrl; // Added profileImageUrl field
    private List<inputMedHistory> medHistory; // Assuming you need this as well

    private UserData() {}

    public static synchronized UserData getInstance() {
        if (instance == null) {
            instance = new UserData();
        }
        return instance;
    }

    // Getter and Setter methods for all fields

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public Uri getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(Uri profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public List<inputMedHistory> getMedHistory() {
        return medHistory;
    }

    public void setMedHistory(List<inputMedHistory> medHistory) {
        this.medHistory = medHistory;
    }
}
