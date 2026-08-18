package com.alya.aplikasilansia.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.BloodPresRepository;
import com.alya.aplikasilansia.data.BloodPressure;
import com.alya.aplikasilansia.data.QuizHistoryItem;
import com.alya.aplikasilansia.data.QuizRepository;
import com.alya.aplikasilansia.data.User;
import com.alya.aplikasilansia.data.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private MutableLiveData<User> userLiveData;
    private MutableLiveData<String> updateResultLiveData;
    private UserRepository userRepository;

    // BARU: repository untuk hasil screening (quiz) & tekanan darah
    private QuizRepository quizRepository;
    private BloodPresRepository bloodPresRepository;

    public ProfileViewModel() {
        userLiveData = new MutableLiveData<>();
        updateResultLiveData = new MutableLiveData<>();
        userRepository = new UserRepository();
        quizRepository = new QuizRepository();
        bloodPresRepository = new BloodPresRepository();
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

    // BARU: ambil riwayat skor tes (screening) milik user yang login,
    // lalu ambil yang paling baru saja (index 0, karena repo sudah sort by date terbaru)
    public LiveData<List<QuizHistoryItem>> getQuizHistory() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) {
            quizRepository.fetchQuizHistory(uid);
        }
        return quizRepository.getQuizHistoryLiveData();
    }

    // BARU: ambil tekanan darah paling terakhir
    public LiveData<BloodPressure> getLatestBloodPressure() {
        return bloodPresRepository.getLatestBloodPressure();
    }
}