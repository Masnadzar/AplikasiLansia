package com.alya.aplikasilansia.data;

import java.util.List;

public class User {
    private String email;
    private String birthDate;
    private String userName;
    private String profileImageUrl; // DIUBAH dari Uri -> String (Firestore tidak bisa auto-map tipe Uri)
    private String caregiver;
    private String maritalStatus;

    private String gender;
    private List<inputMedHistory> medHistory;

    public User() {
        // Konstruktor kosong WAJIB ada untuk DocumentSnapshot.toObject(User.class)
    }
    public User(String email, String birthDate, String userName, String gender, String profileImageUrl, String caregiver, String maritalStatus, List<inputMedHistory> medHistory) {
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

    // DIUBAH: return type dari Uri -> String
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // DIUBAH: parameter dari Uri -> String
    public void setProfileImageUrl(String profileImageUrl) {
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