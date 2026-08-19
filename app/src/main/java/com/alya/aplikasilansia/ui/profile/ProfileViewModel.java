package com.alya.aplikasilansia.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.User;
import com.alya.aplikasilansia.data.UserRepository;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<User> userLiveData;
    private MutableLiveData<String> updateResultLiveData;
    private MutableLiveData<String> deleteResultLiveData;
    private UserRepository userRepository;

    public ProfileViewModel() {
        userLiveData = new MutableLiveData<>();
        updateResultLiveData = new MutableLiveData<>();
        deleteResultLiveData = new MutableLiveData<>();
        userRepository = new UserRepository();
        fetchUser();
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getUpdateResultLiveData() {
        return updateResultLiveData;
    }

    public LiveData<String> getDeleteResultLiveData() {
        return deleteResultLiveData;
    }

    public void fetchUser() {
        userLiveData = userRepository.fetchUser();
    }

    public void updateProfile(String newUserName, String email, String birthDate, Uri profileImageUri) {
        userRepository.updateProfile(newUserName, email, birthDate, profileImageUri, updateResultLiveData);
    }

    public void deleteAccount() {
        userRepository.deleteAccount(deleteResultLiveData);
    }

    public void signOut() {
        userRepository.signOut();
    }
}