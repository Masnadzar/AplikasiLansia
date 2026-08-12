package com.alya.aplikasilansia.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserData {
    private static UserData instance;
    private String email;
    private String password; // Added password field
    private String birthDate;
    private String userName;
    private String gender;
    private String caregiver;
    private String maritalStatus;
    private String profileImageUrl; // Firestore stores this as a String (download URL), not a Uri
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public List<inputMedHistory> getMedHistory() {
        return medHistory;
    }

    public void setMedHistory(List<inputMedHistory> medHistory) {
        this.medHistory = medHistory;
    }

    /**
     * Mengubah data user menjadi Map agar bisa langsung disimpan
     * ke dokumen Firestore, misalnya:
     * firestore.collection("users").document(uid).set(userData.toMap());
     * Field "password" sengaja tidak disertakan karena tidak boleh disimpan
     * di Firestore (password sudah dikelola oleh Firebase Authentication).
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("birthDate", birthDate);
        map.put("userName", userName);
        map.put("gender", gender);
        map.put("caregiver", caregiver);
        map.put("maritalStatus", maritalStatus);
        map.put("profileImageUrl", profileImageUrl);

        List<Map<String, Object>> medHistoryMaps = new ArrayList<>();
        if (medHistory != null) {
            for (inputMedHistory item : medHistory) {
                if (item != null) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("penyakit", item.getPenyakit());
                    itemMap.put("lamanya", item.getLamanya());
                    itemMap.put("lamanyaBulan", item.getLamanyaBulan());
                    medHistoryMaps.add(itemMap);
                }
            }
        }
        map.put("medHistory", medHistoryMaps);

        return map;
    }

    /**
     * Mengisi ulang instance UserData dari sebuah dokumen Firestore, misalnya:
     * userData.fromMap(documentSnapshot.getData());
     */
    @SuppressWarnings("unchecked")
    public void fromMap(Map<String, Object> map) {
        if (map == null) return;

        this.email = (String) map.get("email");
        this.birthDate = (String) map.get("birthDate");
        this.userName = (String) map.get("userName");
        this.gender = (String) map.get("gender");
        this.caregiver = (String) map.get("caregiver");
        this.maritalStatus = (String) map.get("maritalStatus");
        this.profileImageUrl = (String) map.get("profileImageUrl");

        List<inputMedHistory> parsedMedHistory = new ArrayList<>();
        Object rawMedHistory = map.get("medHistory");
        if (rawMedHistory instanceof List) {
            for (Object rawItem : (List<Object>) rawMedHistory) {
                if (rawItem instanceof Map) {
                    Map<String, Object> itemMap = (Map<String, Object>) rawItem;
                    inputMedHistory item = new inputMedHistory();
                    item.setPenyakit((String) itemMap.get("penyakit"));
                    item.setLamanya((String) itemMap.get("lamanya"));
                    item.setLamanyaBulan((String) itemMap.get("lamanyaBulan"));
                    parsedMedHistory.add(item);
                }
            }
        }
        this.medHistory = parsedMedHistory;
    }

    /**
     * Membersihkan data singleton, dipanggil setelah proses registrasi
     * selesai (berhasil disimpan ke Firestore) supaya data lama tidak
     * tertinggal untuk sesi registrasi berikutnya.
     */
    public static synchronized void reset() {
        instance = null;
    }
}