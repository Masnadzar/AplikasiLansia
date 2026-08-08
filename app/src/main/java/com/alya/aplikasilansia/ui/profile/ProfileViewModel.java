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
    private UserRepository userRepository;

    public ProfileViewModel() {
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

    public void signOut() {
        userRepository.signOut();
    }
}


