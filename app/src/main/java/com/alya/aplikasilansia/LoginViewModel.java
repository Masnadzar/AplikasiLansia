package com.alya.aplikasilansia;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {
    private UserRepository userRepository;
    public MutableLiveData<FirebaseUser> userLiveData;
    public MutableLiveData<String> errorLiveData;

    public LoginViewModel() {
        userRepository = new UserRepository();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public void login(String email, String password) {
        userRepository.login(email, password, userLiveData, errorLiveData);
    }
}


