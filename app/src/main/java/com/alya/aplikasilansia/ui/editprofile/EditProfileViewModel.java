package com.alya.aplikasilansia.ui.editprofile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.User;
import com.alya.aplikasilansia.data.UserRepository;
import com.alya.aplikasilansia.data.inputMedHistory;

import java.util.List;

public class EditProfileViewModel extends ViewModel {
    private MutableLiveData<User> userLiveData;
    private MutableLiveData<String> updateResultLiveData;
    private UserRepository userRepository;

    public EditProfileViewModel() {
        userLiveData = new MutableLiveData<>();
        updateResultLiveData = new MutableLiveData<>();
        userRepository = new UserRepository();
        fetchUser();
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getUpdateResultLiveData() {
        return updateResultLiveData;
    }

    public void fetchUser() {
        userLiveData = userRepository.fetchUser();
    }

    public void updateProfile(String newUserName, String email, String birthDate, Uri profileImageUri) {
        userRepository.updateProfile(newUserName, email, birthDate, profileImageUri, updateResultLiveData);
    }

    public void updateMedRecord(List<inputMedHistory> medHistory){
        userRepository.updateMedHistory(medHistory, updateResultLiveData);
    }
    public void updateHealthData2(String caregiver, String maritalStatus) {
        userRepository.updateMedData(caregiver, maritalStatus, updateResultLiveData);
    }
}
