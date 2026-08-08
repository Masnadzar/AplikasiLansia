package com.alya.aplikasilansia.ui.editprofile;

import com.alya.aplikasilansia.data.inputMedHistory;

import java.util.List;

public interface OnSaveEditListener {
    void onSavePersonalData(String newUsername, String newBirthdate);
    void onSaveHealthData(String newCaregiver, String newStatus, List<inputMedHistory> medHistoryList);
}

